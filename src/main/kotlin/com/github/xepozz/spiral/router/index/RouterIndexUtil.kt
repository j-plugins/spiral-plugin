package com.github.xepozz.spiral.router.index

import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression

object RouterIndexUtil {
    fun parseMethods(element: PsiElement?): Collection<String> = when (element) {
        is ArrayCreationExpression -> element.children.flatMap { parseMethods(it) }
        is StringLiteralExpression -> listOf(parseContent(element))
        // check for class constant
        null -> emptyList() // default verbs
        else -> listOf(parseContent(element))
    }

    fun parseContent(element: PsiElement?) = when (element) {
        null -> ""
        is StringLiteralExpression -> StringUtil.unquoteString(element.contents)
        else -> StringUtil.unquoteString(element.text)
    }
}