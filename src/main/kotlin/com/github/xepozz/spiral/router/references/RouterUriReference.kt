package com.github.xepozz.spiral.router.references

import com.github.xepozz.spiral.SpiralIcons
import com.github.xepozz.spiral.router.index.RouterIndexUtil
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.impl.source.tree.injected.changesHandler.contentRange
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression

class RouterUriReference(
    element: StringLiteralExpression,
) : PsiReferenceBase<PsiElement>(element, element.contentRange.shiftLeft(element.textOffset)) {
    override fun resolve(): PsiElement? {
        return null
    }

    override fun isSoft() = true

    override fun getVariants(): Array<out Any?> {
        return RouterIndexUtil
            .getAllRoutes(element.project)
            .map { route ->
                LookupElementBuilder.create(route.uri)
                    .withTypeText(route.name)
                    .withTailText(" [${route.group}]")
                    .withIcon(SpiralIcons.SPIRAL)
            }
            .toTypedArray()
    }
}