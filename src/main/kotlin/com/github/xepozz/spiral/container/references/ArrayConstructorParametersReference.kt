package com.github.xepozz.spiral.container.references

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression
import kotlin.collections.toList

class ArrayConstructorParametersReference(
    val classFqn: String,
    val property: String,
    element: StringLiteralExpression,
) : PsiPolyVariantReferenceBase<PsiElement>(element) {
    override fun getVariants(): Array<out Any> {
        println("lookup for class: $classFqn, property: $property")
        val project = element.project
        val classes = PhpIndex.getInstance(project).getClassesByFQN(classFqn)
        if (classes.isEmpty()) return emptyArray()

        return classes
            .mapNotNull { it.constructor }
            .apply { println("variants: $this") }
            .flatMap { it.parameters.toList() }
//            .map { LookupElementBuilder.create(it) }
            .toTypedArray()
//            .run { this.toTypedArray() }
    }

    override fun isSoft() = true
//    override fun getRangeInElement(): TextRange =
    override fun calculateDefaultRangeInElement(): TextRange? {
        return TextRange(1, element.textLength - 1)
    }
    override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
        val project = element.project
        val classes = PhpIndex.getInstance(project).getClassesByFQN(classFqn)
        if (classes.isEmpty()) return emptyArray()

//        return emptyArray()
        return classes
            .mapNotNull { it.constructor?.parameters }
            .flatMap { it.toList() }
            .filter { it.name == property }
            .run { PsiElementResolveResult.createResults(this) }

//            .flatMap { it.fields }
//            .filter { it.name == property }
//            .run { PsiElementResolveResult.createResults(this) }
    }

}