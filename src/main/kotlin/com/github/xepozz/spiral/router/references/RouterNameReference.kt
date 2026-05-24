package com.github.xepozz.spiral.router.references

import com.github.xepozz.spiral.SpiralBundle
import com.github.xepozz.spiral.SpiralIcons
import com.github.xepozz.spiral.php.contentRange
import com.github.xepozz.spiral.router.index.RouterIndexUtil
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression

class RouterNameReference(
    element: StringLiteralExpression,
) : PsiReferenceBase<PsiElement>(element, element.contentRange.shiftLeft(element.textOffset)) {
    override fun resolve(): PsiElement? {
        return null
    }

    override fun isSoft() = true

    override fun getVariants(): Array<out Any?> {
        val project = element.project
        if (DumbService.isDumb(project)) return emptyArray()

        val rootLabel = SpiralBundle.message("endpoints.group.root")
        return RouterIndexUtil
            .getAllRoutes(project)
            .asSequence()
            .filter { it.name != null }
            .distinctBy { it.name }
            .map { route ->
                LookupElementBuilder.create(route.name!!)
                    .withLookupString(route.name.lowercase())
                    .withTypeText(route.uri)
                    .withTailText(" [${route.group ?: rootLabel}]")
                    .withIcon(SpiralIcons.SPIRAL)
            }
            .toList()
            .toTypedArray()
    }
}
