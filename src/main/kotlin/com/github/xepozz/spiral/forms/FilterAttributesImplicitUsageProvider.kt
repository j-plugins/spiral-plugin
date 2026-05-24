package com.github.xepozz.spiral.forms

import com.github.xepozz.spiral.SpiralFrameworkClasses
import com.github.xepozz.spiral.php.hasSuperClass
import com.intellij.codeInsight.daemon.ImplicitUsageProvider
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.elements.Field

/**
 * For some reason [ImplicitUsageProvider] doesn't handle Fields, but hope it will one day
 */
class FilterAttributesImplicitUsageProvider : ImplicitUsageProvider {
    override fun isImplicitUsage(element: PsiElement) = isAttributesFilterField(element)

    /**
     * Fields of an [SpiralFrameworkClasses.ATTRIBUTES_FILTER] subclass are read by the
     * framework when the validated DTO is consumed, so suppress "Unused field" warnings.
     */
    override fun isImplicitRead(element: PsiElement) = isAttributesFilterField(element)

    override fun isImplicitWrite(element: PsiElement) = isAttributesFilterField(element)

    private fun isAttributesFilterField(element: PsiElement): Boolean = when (element) {
        is Field -> element.containingClass?.hasSuperClass(SpiralFrameworkClasses.ATTRIBUTES_FILTER) == true
        else -> false
    }
}
