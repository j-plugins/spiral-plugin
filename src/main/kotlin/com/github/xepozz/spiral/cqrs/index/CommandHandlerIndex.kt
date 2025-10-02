package com.github.xepozz.spiral.cqrs.index

import com.github.xepozz.spiral.SpiralFrameworkClasses
import com.github.xepozz.spiral.index.AbstractIndex
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ID
import com.intellij.util.io.EnumeratorStringDescriptor
import com.jetbrains.php.lang.PhpFileType
import com.jetbrains.php.lang.psi.elements.ClassReference
import com.jetbrains.php.lang.psi.elements.Method
import com.jetbrains.php.lang.psi.elements.PhpAttribute

private typealias CommandHandlerType = String

class CommandHandlerIndex : AbstractIndex<CommandHandlerType>() {
    companion object {
        val key = ID.create<String, CommandHandlerType>("Spiral.Cqrs.CommandHandler")

        fun getAll(project: Project): Map<String, String> {
            val scope = GlobalSearchScope.allScope(project)

            val prototypes = getPrototypes(project)

            val fileBasedIndex = FileBasedIndex.getInstance()

            return prototypes
                .map { it to (fileBasedIndex.getValues(key, it, scope).firstOrNull() ?: "") }
                .associate { it }
//            fileBasedIndex.getValues(key, prototypes, GlobalSearchScope.allScope(project))
        }

        fun getPrototypes(project: Project): Collection<String> {
            val fileBasedIndex = FileBasedIndex.getInstance()

            return fileBasedIndex.getAllKeys(key, project)
        }

        fun getPrototypeClass(prototype: String, project: Project): CommandHandlerType? {
            val fileBasedIndex = FileBasedIndex.getInstance()

            return fileBasedIndex
                .getValues(key, prototype, GlobalSearchScope.allScope(project))
                .firstOrNull()
        }
    }

    override fun getVersion() = 2

    override fun getName() = key

    override fun getValueExternalizer() = EnumeratorStringDescriptor.INSTANCE

    override fun getInputFilter() = FileBasedIndex.InputFilter { it.fileType == PhpFileType.INSTANCE }

    override fun getIndexer() = DataIndexer<String, CommandHandlerType, FileContent> { inputData ->
        inputData
            .psiFile
            .let { PsiTreeUtil.findChildrenOfType(it, PhpAttribute::class.java) }
            .find { it.fqn == SpiralFrameworkClasses.COMMAND_HANDLER }
            .let { it?.owner as? Method }
            ?.let {
                val command = it.getParameter(0)
                    .let { PsiTreeUtil.findChildrenOfType(it, ClassReference::class.java) }
                    .firstOrNull()
                    ?.fqn
                    ?: ""
                mapOf(it.fqn to command)
            }
            ?: emptyMap()
//            .associate { it.first to it.second }
    }

}