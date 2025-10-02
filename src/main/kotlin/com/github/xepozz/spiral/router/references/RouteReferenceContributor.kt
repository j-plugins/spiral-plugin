package com.github.xepozz.spiral.router.references

import com.github.xepozz.spiral.SpiralFrameworkClasses
import com.github.xepozz.spiral.common.references.AttributesUtil.getPsiArgument
import com.github.xepozz.spiral.php.patterns.AttributeFqnCondition
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.util.ProcessingContext
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression
import com.jetbrains.php.lang.psi.elements.ParameterList
import com.jetbrains.php.lang.psi.elements.PhpAttribute
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression

class RouteReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(StringLiteralExpression::class.java)
                .withParent(PlatformPatterns.psiElement(ParameterList::class.java))
                .withSuperParent(
                    2,
                    PlatformPatterns.psiElement(PhpAttribute::class.java)
                        .with(AttributeFqnCondition(StandardPatterns.string().oneOf(SpiralFrameworkClasses.ROUTE)))
                ),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<out PsiReference> {
                    val element = element as? StringLiteralExpression ?: return PsiReference.EMPTY_ARRAY
                    val attribute = element.parent.parent as? PhpAttribute ?: return PsiReference.EMPTY_ARRAY

                    return when (element) {
                        attribute.getPsiArgument("uri", 0) -> arrayOf(RouterUriReference(element))
                        attribute.getPsiArgument("name", 1) -> arrayOf(RouterNameReference(element))
                        attribute.getPsiArgument("methods", 2) -> arrayOf(RouterMethodsReference(element))
                        attribute.getPsiArgument("group", 4) -> arrayOf(RouterGroupReference(element))
                        else -> PsiReference.EMPTY_ARRAY
                    }
                }
            }
        )

        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(StringLiteralExpression::class.java)
                .withSuperParent(2, ArrayCreationExpression::class.java)
                .withSuperParent(3, ParameterList::class.java)
                .withSuperParent(
                    4,
                    PlatformPatterns.psiElement(PhpAttribute::class.java)
                        .with(AttributeFqnCondition(StandardPatterns.string().oneOf(SpiralFrameworkClasses.ROUTE)))
                ),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<out PsiReference> {
                    val element = element as? StringLiteralExpression ?: return PsiReference.EMPTY_ARRAY
                    val arrayCreation = element.parent.parent as? ArrayCreationExpression ?: return PsiReference.EMPTY_ARRAY
                    val attribute = element.parent.parent.parent.parent as? PhpAttribute ?: return PsiReference.EMPTY_ARRAY

                    return when (arrayCreation) {
                        attribute.getPsiArgument("methods", 2) -> arrayOf(RouterMethodsReference(element))
                        else -> PsiReference.EMPTY_ARRAY
                    }
                }
            }
        )
    }
}

