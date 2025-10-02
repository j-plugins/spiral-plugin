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
    override fun isImplicitUsage(element: PsiElement) = when (element) {
        is Field -> element.containingClass?.hasSuperClass(SpiralFrameworkClasses.ATTRIBUTES_FILTER) == true
        else -> false
    }
//        .apply { println("element: $element: ${element.text}, isImplicitUsage: $this") }

    override fun isImplicitRead(element: PsiElement) = false

    override fun isImplicitWrite(element: PsiElement) = true
}