package com.github.xepozz.spiral.views.references

import com.github.xepozz.spiral.SpiralFrameworkClasses
import com.github.xepozz.spiral.php.hasSignature
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.util.ProcessingContext
import com.jetbrains.php.lang.psi.elements.MethodReference
import com.jetbrains.php.lang.psi.elements.ParameterList
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression

class ViewReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(StringLiteralExpression::class.java)
                .withParent(PlatformPatterns.psiElement(ParameterList::class.java))
                .withSuperParent(
                    2,
                    PlatformPatterns.psiElement(MethodReference::class.java)
                ),
            object : PsiReferenceProvider() {
                val SIGNATURE = "#M#C${SpiralFrameworkClasses.VIEWS_INTERFACE}.render"
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<out PsiReference?> {
                    val element = element as StringLiteralExpression
                    val methodReference = element.parent.parent as MethodReference

                    if (methodReference.getParameter(0) != element) {
//                        println("${element} is not a parameter")
                        return emptyArray()
                    }
                    if (!methodReference.hasSignature(SIGNATURE)) {
//                        println("${methodReference.getSignatures()} does not have $SIGNATURE signature")
                        return emptyArray()
                    }

//                    println("reference: $element: ${element.text}")

                    return arrayOf(
                        ViewNamespaceReference(element.contents, element),
                        ViewFileReference(element.contents, element),
                    )
                }
            }
        )
    }
}