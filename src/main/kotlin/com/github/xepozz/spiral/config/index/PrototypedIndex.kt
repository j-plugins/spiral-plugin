package com.github.xepozz.spiral.config.index

import com.github.xepozz.spiral.index.AbstractIndex
import com.github.xepozz.spiral.SpiralFrameworkClasses
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ID
import com.intellij.util.io.EnumeratorStringDescriptor
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.PhpFileType
import com.jetbrains.php.lang.psi.elements.PhpAttribute
import com.jetbrains.php.lang.psi.elements.PhpClass

private typealias PrototypedIndexType = String

class PrototypedIndex : AbstractIndex<PrototypedIndexType>() {
    companion object {
        val key = ID.create<String, PrototypedIndexType>("Spiral.Prototyped")

        fun getPrototypes(project: Project): Collection<String> {
            val fileBasedIndex = FileBasedIndex.getInstance()

            return fileBasedIndex.getAllKeys(key, project)
        }

        fun getPrototypeClass(prototype: String, project: Project): PrototypedIndexType? {
            val fileBasedIndex = FileBasedIndex.getInstance()

            return fileBasedIndex
                .getValues(key, prototype, GlobalSearchScope.allScope(project))
                .firstOrNull()
        }
    }

    override fun getVersion() = 1

    override fun getName() = key

    override fun getValueExternalizer() = EnumeratorStringDescriptor.INSTANCE

    override fun getInputFilter() = FileBasedIndex.InputFilter { it.fileType == PhpFileType.INSTANCE }

    override fun getIndexer() = DataIndexer<String, PrototypedIndexType, FileContent> { inputData ->
        inputData
            .psiFile
            .let { PsiTreeUtil.findChildrenOfType(it, PhpAttribute::class.java) }
            .filter { it.fqn == SpiralFrameworkClasses.PROTOTYPED }
            .mapNotNull { attribute ->
                attribute.arguments
                    .firstOrNull { it.name == "property" || it.name.isEmpty() }
                    ?.argument
                    ?.value to attribute.owner as? PhpClass
            }
            .filter { it.second != null && !it.first.isNullOrEmpty() }
            .map { StringUtil.unquoteString(it.first!!) to it.second!!.fqn }
            .associate { it }
//            .associate { it.first to it.second }
    }

}