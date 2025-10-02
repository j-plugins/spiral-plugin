package com.github.xepozz.spiral.cqrs

import com.github.xepozz.spiral.SpiralFrameworkClasses
import com.github.xepozz.spiral.SpiralIcons
import com.github.xepozz.spiral.php.hasInterface
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.psi.PsiElement
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.psi.elements.PhpClass

class CqrsHandlersLineMarkerProvider : RelatedItemLineMarkerProvider() {
    override fun getLineMarkerInfo(element: PsiElement): RelatedItemLineMarkerInfo<*>? {
        val element = element as? PhpClass ?: return null

        val isCommand = element.hasInterface(SpiralFrameworkClasses.CQRS_COMMAND)
        val isQuery = element.hasInterface(SpiralFrameworkClasses.CQRS_QUERY)
        if (!isCommand && !isQuery) return null

        val nameIdentifier = element.nameIdentifier ?: return null

        val project = element.project
        val phpIndex = PhpIndex.getInstance(project)

        val classes = if (isQuery) {
            CqrsIndexUtil.findQueryHandlers(element.fqn, project)
        } else {
            CqrsIndexUtil.findCommandHandlers(element.fqn, project)
        }
        return classes
            .map { toClassFqn(it) }
            .let { classes ->
                val targets: NotNullLazyValue<Collection<PsiElement>> = NotNullLazyValue
                    .createValue { classes.flatMap { phpIndex.getAnyByFQN(it) } }

                // todo: replace with more suitable icon
                NavigationGutterIconBuilder.create(SpiralIcons.SPIRAL)
                    .setTargets(targets)
                    .setTooltipText("Navigate to handler")
                    .createLineMarkerInfo(nameIdentifier)
            }
    }

    /**
     * 99% command handler targets to __invoke method, so we can just remove method part of FQN.
     */
    private fun toClassFqn(fqn: String): String = fqn.substringBeforeLast('.')
}