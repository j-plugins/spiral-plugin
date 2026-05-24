package com.github.xepozz.spiral.router

import com.github.xepozz.spiral.SpiralFrameworkClasses
import com.intellij.codeInsight.daemon.ImplicitUsageProvider
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.elements.PhpClass

class RoutesImplicitUsageProvider : ImplicitUsageProvider {
    override fun isImplicitUsage(element: PsiElement): Boolean {
        if (element !is PhpClass) return false
        // Spiral places `#[Route]` on controller methods; iterate methods directly
        // rather than walking every PSI descendant for cheaper class-level checks.
        return element.methods.any { method ->
            method.getAttributes(SpiralFrameworkClasses.ROUTE).isNotEmpty()
        }
    }

    // Routes are wired at runtime by Spiral's router; the IDE never observes
    // direct reads or writes to the controller class itself.
    override fun isImplicitRead(element: PsiElement) = false

    override fun isImplicitWrite(element: PsiElement) = false

    override fun isClassWithCustomizedInitialization(element: PsiElement) = true
}
