package com.github.xepozz.spiral.cqrs

import com.github.xepozz.spiral.SpiralFrameworkClasses
import com.github.xepozz.spiral.SpiralIcons
import com.github.xepozz.spiral.php.hasInterface
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.psi.PsiElement
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.psi.elements.PhpClass

class CqrsHandlersLineMarkerProvider : RelatedItemLineMarkerProvider() {
    override fun getLineMarkerInfo(element: PsiElement): RelatedItemLineMarkerInfo<*>? {
        // IntelliJ Platform invokes line marker providers on leaf PSI elements (per the
        // "anchor to identifier" guideline). The class-name leaf is the identifier; its
        // parent is the PhpClass we actually want to inspect.
        val phpClass = element.parent as? PhpClass ?: return null
        if (phpClass.nameIdentifier != element) return null

        val isCommand = phpClass.hasInterface(SpiralFrameworkClasses.CQRS_COMMAND)
        val isQuery = phpClass.hasInterface(SpiralFrameworkClasses.CQRS_QUERY)
        if (!isCommand && !isQuery) return null

        val project = phpClass.project
        if (DumbService.isDumb(project)) return null
        val phpIndex = PhpIndex.getInstance(project)

        val classes = if (isQuery) {
            CqrsIndexUtil.findQueryHandlers(phpClass.fqn, project)
        } else {
            CqrsIndexUtil.findCommandHandlers(phpClass.fqn, project)
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
                    .createLineMarkerInfo(element)
            }
    }

    /**
     * 99% command handler targets to __invoke method, so we can just remove method part of FQN.
     */
    private fun toClassFqn(fqn: String): String = fqn.substringBeforeLast('.')
}