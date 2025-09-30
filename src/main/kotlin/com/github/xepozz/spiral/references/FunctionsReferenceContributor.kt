package com.github.xepozz.spiral.references

import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.util.ProcessingContext
import com.jetbrains.php.lang.psi.elements.FunctionReference
import com.jetbrains.php.lang.psi.elements.ParameterList
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression

class FunctionsReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(StringLiteralExpression::class.java)
                .withParent(ParameterList::class.java)
                .withSuperParent(
                    2,
                    PlatformPatterns
                        .psiElement(FunctionReference::class.java)
//                        .withName("directory")
                ),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<out PsiReference> {
                    val element = element as? StringLiteralExpression ?: return PsiReference.EMPTY_ARRAY
                    val function = element.parent.parent as? FunctionReference ?: return PsiReference.EMPTY_ARRAY

//                    println("directory: ${element.text}, function: ${function.name}")

                    return when (function.name) {
                        "directory" -> arrayOf(DirectoryReference(element.contents, element.textRangeInParent, element))
                        else -> PsiReference.EMPTY_ARRAY
                    }
//                        .apply { println("references: $this") }
                }
            }
        )
    }
}

