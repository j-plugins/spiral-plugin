package com.github.xepozz.spiral.views.index

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex

object ViewsNamespaceIndexUtil {
    fun getNamespace(namespaceName: String, project: Project): String? {
        if (DumbService.isDumb(project)) return null
        val fileBasedIndex = FileBasedIndex.getInstance()

        return fileBasedIndex
            .getValues(ViewsNamespaceIndex.key, namespaceName, GlobalSearchScope.allScope(project))
            .firstOrNull()
    }

    fun getAllNamespaces(project: Project): Map<String, String> {
        if (DumbService.isDumb(project)) return emptyMap()
        val fileBasedIndex = FileBasedIndex.getInstance()

        return fileBasedIndex
            .getAllKeys(ViewsNamespaceIndex.key, project)
            .let { it.associateWith { getNamespace(it, project) ?: "" } }

    }
}