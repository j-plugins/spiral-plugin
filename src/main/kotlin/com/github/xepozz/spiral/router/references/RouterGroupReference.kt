package com.github.xepozz.spiral.router.references

import com.github.xepozz.spiral.SpiralIcons
import com.github.xepozz.spiral.php.contentRange
import com.github.xepozz.spiral.router.index.RouterIndexUtil
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression

class RouterGroupReference(
    element: StringLiteralExpression,
) : PsiReferenceBase<PsiElement>(element, element.contentRange.shiftLeft(element.textOffset)) {
    override fun resolve(): PsiElement? {
        return null
    }

    override fun isSoft() = true

    override fun getVariants(): Array<out Any?> {
        val project = element.project
        if (DumbService.isDumb(project)) return emptyArray()

        // Routes without an explicit group implicitly belong to the "Root" group;
        // exclude null groups here because the literal value the user would type
        // is the explicit name, not the implicit fallback.
        return RouterIndexUtil
            .getAllRoutes(project)
            .mapNotNull { it.group }
            .toSet()
            .map { group ->
                LookupElementBuilder.create(group)
                    .withLookupString(group.lowercase())
                    .withIcon(SpiralIcons.SPIRAL)
            }
            .toTypedArray()
    }
}
