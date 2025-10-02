package com.github.xepozz.spiral.cqrs

import com.github.xepozz.spiral.SpiralFrameworkClasses
import com.intellij.codeInsight.daemon.ImplicitUsageProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.psi.elements.PhpAttribute
import com.jetbrains.php.lang.psi.elements.PhpClass

class CqrsHandlersImplicitUsageProvider : ImplicitUsageProvider {
    companion object {
        private val handlers = listOf(
            SpiralFrameworkClasses.CQRS_COMMAND_HANDLER,
            SpiralFrameworkClasses.CQRS_QUERY_HANDLER,
        )
    }

    override fun isImplicitUsage(element: PsiElement) = when (element) {
        is PhpClass -> PsiTreeUtil
            .findChildrenOfType(element, PhpAttribute::class.java)
            .any { handlers.contains(it.fqn) }

        else -> false
    }

    override fun isImplicitRead(element: PsiElement) = false

    override fun isImplicitWrite(element: PsiElement) = false

    override fun isClassWithCustomizedInitialization(element: PsiElement) = true
}