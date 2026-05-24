package com.github.xepozz.spiral.container.references

import com.github.xepozz.spiral.SpiralFrameworkClasses
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.util.ProcessingContext
import com.jetbrains.php.lang.psi.elements.ArrayHashElement
import com.jetbrains.php.lang.psi.elements.ClassConstantReference
import com.jetbrains.php.lang.psi.elements.ClassReference
import com.jetbrains.php.lang.psi.elements.NewExpression
import com.jetbrains.php.lang.psi.elements.ParameterList
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression

class ContainerReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        // Parent chain for the matched StringLiteralExpression:
        //   StringLiteral (key) → ArrayHashElement → ArrayCreationExpression →
        //   ParameterList (whose first child is a ClassConstantReference like Foo::class) →
        //   NewExpression (new Autowire(Foo::class, [...])).
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(StringLiteralExpression::class.java)
                .withSuperParent(2, ArrayHashElement::class.java)
                .withSuperParent(
                    4,
                    PlatformPatterns.psiElement(ParameterList::class.java)
                        .withFirstChild(
                            PlatformPatterns.psiElement(ClassConstantReference::class.java)
                        )
                )
                .withSuperParent(5, NewExpression::class.java),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<out PsiReference> {
                    val stringLiteral = element as? StringLiteralExpression ?: return PsiReference.EMPTY_ARRAY
                    val arrayHashElement =
                        stringLiteral.parent.parent as? ArrayHashElement ?: return PsiReference.EMPTY_ARRAY
                    val newExpression =
                        arrayHashElement.parent.parent.parent as? NewExpression ?: return PsiReference.EMPTY_ARRAY
                    if (stringLiteral != arrayHashElement.key) return PsiReference.EMPTY_ARRAY
                    val classReference = newExpression.classReference ?: return PsiReference.EMPTY_ARRAY
                    if (classReference.fqn != SpiralFrameworkClasses.AUTOWIRE) return PsiReference.EMPTY_ARRAY
                    val autowiringClassReference = (newExpression.parameters.getOrNull(0) as? ClassConstantReference)
                        ?.classReference as? ClassReference
                        ?: return PsiReference.EMPTY_ARRAY
                    val autowiringClass = autowiringClassReference.fqn ?: return PsiReference.EMPTY_ARRAY

                    return arrayOf(
                        ArrayConstructorParametersReference(autowiringClass, stringLiteral.contents, stringLiteral)
                    )
                }
            }
        )
    }
}
