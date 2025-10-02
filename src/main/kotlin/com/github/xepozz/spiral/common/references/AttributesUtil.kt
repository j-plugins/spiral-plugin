package com.github.xepozz.spiral.common.references

import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.elements.PhpAttribute

object AttributesUtil {
    fun PhpAttribute.getPsiArgument(name: String, index: Int): PsiElement? {
        val argument = arguments.find { it.name == name } ?: arguments.getOrNull(index)

        val argumentIndex = argument?.argument?.argumentIndex ?: index

        return parameters.getOrNull(argumentIndex)
    }
}