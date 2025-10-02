package com.github.xepozz.spiral.router.references

import com.github.xepozz.spiral.SpiralIcons
import com.github.xepozz.spiral.router.index.RouterIndexUtil
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.impl.source.tree.injected.changesHandler.contentRange
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression

class RouterNameReference(
    element: StringLiteralExpression,
) : PsiReferenceBase<PsiElement>(element, element.contentRange.shiftLeft(element.textOffset)) {
    override fun resolve(): PsiElement? {
        return null
    }

    override fun isSoft() = true

    override fun getVariants(): Array<out Any?> {
        return RouterIndexUtil
            .getAllRoutes(element.project)
            .mapNotNull { route ->
                if (route.name == null) return@mapNotNull null

                LookupElementBuilder.create(route.name)
                    .withTypeText(route.uri)
                    .withTailText(" [${route.group}]")
                    .withIcon(SpiralIcons.SPIRAL)
            }
            .toTypedArray()
    }
}