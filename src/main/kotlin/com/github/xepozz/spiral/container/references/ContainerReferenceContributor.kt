package com.github.xepozz.spiral.container.references

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
                    val element = element as? StringLiteralExpression ?: return PsiReference.EMPTY_ARRAY
                    val arrayHashElement = element.parent.parent as? ArrayHashElement ?: return PsiReference.EMPTY_ARRAY
                    val newExpression =
                        arrayHashElement.parent.parent.parent as? NewExpression ?: return PsiReference.EMPTY_ARRAY
                    if (element != arrayHashElement.key) return PsiReference.EMPTY_ARRAY
                    val classReference = newExpression.classReference ?: return PsiReference.EMPTY_ARRAY
                    if (classReference.fqn != "\\Spiral\\Core\\Container\\Autowire") return PsiReference.EMPTY_ARRAY
                    val autowiringClassReference = (newExpression.parameters[0] as? ClassConstantReference)
                        ?.classReference as? ClassReference
                        ?: return PsiReference.EMPTY_ARRAY
                    val autowiringClass = autowiringClassReference.fqn ?: return PsiReference.EMPTY_ARRAY

//                    \Spiral\Core\Container\Autowire::__construct

//                    println("property: ${element.text}, arrayHashElement: ${arrayHashElement.key}")

                    return arrayOf(ArrayConstructorParametersReference(autowiringClass, element.contents, element))
                }
            }
        )
    }
}

