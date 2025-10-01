package com.github.xepozz.spiral.views.index

import com.github.xepozz.spiral.index.AbstractIndex
import com.github.xepozz.spiral.SpiralFrameworkClasses
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ID
import com.intellij.util.io.EnumeratorStringDescriptor
import com.jetbrains.php.PhpWorkaroundUtil
import com.jetbrains.php.lang.PhpFileType
import com.jetbrains.php.lang.PhpLangUtil
import com.jetbrains.php.lang.psi.elements.ConcatenationExpression
import com.jetbrains.php.lang.psi.elements.MethodReference
import com.jetbrains.php.lang.psi.elements.ParameterList
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression
import com.jetbrains.php.lang.psi.elements.Variable
import com.jetbrains.php.lang.psi.elements.impl.PhpFilePathUtils
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeSignatureKey

object ViewsNamespaceIndexUtil {
    fun getNamespace(namespaceName: String, project: Project): String? {
        val fileBasedIndex = FileBasedIndex.getInstance()

        return fileBasedIndex
            .getValues(ViewsNamespaceIndex.key, namespaceName, GlobalSearchScope.allScope(project))
            .firstOrNull()
    }

    fun getAllNamespaces(project: Project): Map<String, String> {
        val fileBasedIndex = FileBasedIndex.getInstance()

        return fileBasedIndex
            .getAllKeys(ViewsNamespaceIndex.key, project)
            .let { it.associateWith { getNamespace(it, project) ?: "" } }

    }
}