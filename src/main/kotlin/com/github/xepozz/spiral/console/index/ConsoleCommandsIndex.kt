package com.github.xepozz.spiral.console.index

import com.github.xepozz.spiral.SpiralFrameworkClasses
import com.github.xepozz.spiral.SpiralViewUtil
import com.github.xepozz.spiral.index.AbstractIndex
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ID
import com.intellij.util.io.EnumeratorStringDescriptor
import com.jetbrains.php.lang.PhpFileType
import com.jetbrains.php.lang.psi.elements.PhpAttribute

private typealias ConsoleCommandsIndexType = String

class ConsoleCommandsIndex : AbstractIndex<ConsoleCommandsIndexType>() {
    companion object {
        val key = ID.create<String, ConsoleCommandsIndexType>("Spiral.ConsoleCommands")
    }

    override fun getVersion() = 2

    override fun getName() = key

    override fun getValueExternalizer() = EnumeratorStringDescriptor.INSTANCE

    override fun getInputFilter() = FileBasedIndex.InputFilter {
        it.fileType == PhpFileType.INSTANCE && !it.name.endsWith(SpiralViewUtil.VIEW_SUFFIX)
    }

    override fun getIndexer() = DataIndexer<String, ConsoleCommandsIndexType, FileContent> { inputData ->
        inputData
            .psiFile
            .let { PsiTreeUtil.findChildrenOfType(it, PhpAttribute::class.java) }
            .filter { it.fqn == SpiralFrameworkClasses.AS_COMMAND }
            .mapNotNull { attribute ->
                attribute.arguments
                    .firstOrNull { it.name == "name" || it.name.isEmpty() }
                    ?.argument
                    ?.value
            }
            .map { StringUtil.unquoteString(it) }
            .associateBy { it }
//            .apply { println("file: ${inputData.file}, result: $this") }
    }
}