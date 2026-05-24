package com.github.xepozz.spiral.cqrs

import com.github.xepozz.spiral.cqrs.index.CqrsCommandIndex
import com.github.xepozz.spiral.cqrs.index.CqrsQueryIndex
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex

object CqrsIndexUtil {
    fun findCommandHandlers(command: String, project: Project): Collection<String> {
        if (DumbService.isDumb(project)) return emptyList()
        val fileBasedIndex = FileBasedIndex.getInstance()

        return fileBasedIndex
            .getValues(CqrsCommandIndex.key, command, GlobalSearchScope.allScope(project))
    }

    fun findQueryHandlers(command: String, project: Project): Collection<String> {
        if (DumbService.isDumb(project)) return emptyList()
        val fileBasedIndex = FileBasedIndex.getInstance()

        return fileBasedIndex
            .getValues(CqrsQueryIndex.key, command, GlobalSearchScope.allScope(project))
    }
}