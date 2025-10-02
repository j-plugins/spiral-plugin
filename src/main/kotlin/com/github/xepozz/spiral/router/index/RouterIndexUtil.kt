package com.github.xepozz.spiral.router.index

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression

object RouterIndexUtil {
    val ALL_VERBS = listOf("GET", "POST", "PUT", "PATCH", "OPTIONS", "HEAD", "DELETE")

    // todo: check for ClasConstantReference
    fun parseMethods(element: PsiElement?): Collection<String> = when (element) {
        is ArrayCreationExpression -> element.children.flatMap { parseMethods(it) }
        is StringLiteralExpression -> listOf(parseContent(element))
        null -> ALL_VERBS
        else -> listOf(parseContent(element))
    }

    fun parseContent(element: PsiElement?) = when (element) {
        null -> ""
        is StringLiteralExpression -> StringUtil.unquoteString(element.contents)
        else -> StringUtil.unquoteString(element.text)
    }

    fun getAllRoutes(project: Project): Collection<Route> {
        val fileBasedIndex = FileBasedIndex.getInstance()
        val allScope = GlobalSearchScope.allScope(project)

        return fileBasedIndex
            .getAllKeys(RouterUrlsIndex.key, project)
            .flatMap {
                fileBasedIndex
                    .getValues(RouterUrlsIndex.key, it, allScope)
            }
    }
}