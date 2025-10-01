package com.github.xepozz.spiral.prototyped

import com.github.xepozz.spiral.SpiralFrameworkClasses
import com.github.xepozz.spiral.php.patterns.AttributeFqnCondition
import com.intellij.codeInsight.template.PsiElementResult
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementRef
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.psi.PsiReferenceWrapper
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.jetbrains.php.lang.patterns.PhpPatterns
import com.jetbrains.php.lang.psi.elements.FieldReference
import com.jetbrains.php.lang.psi.elements.ParameterList
import com.jetbrains.php.lang.psi.elements.PhpAttribute
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression

class PrototypedAttributeReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(StringLiteralExpression::class.java)
                .withParent(PlatformPatterns.psiElement(ParameterList::class.java))
                .withSuperParent(
                    2,
                    PlatformPatterns.psiElement(PhpAttribute::class.java)
                        .with(AttributeFqnCondition(StandardPatterns.string().oneOf(SpiralFrameworkClasses.PROTOTYPED)))
                ),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<out PsiReference> {
//                    println("reference: $element: ${element.text}")

                    val phpClass = PsiTreeUtil
                        .getParentOfType(element, PhpClass::class.java)
                        ?: return PsiReference.EMPTY_ARRAY

                    return arrayOf(PsiReferenceBase.createSelfReference(element, phpClass))
                }
            }
        )
    }
}

