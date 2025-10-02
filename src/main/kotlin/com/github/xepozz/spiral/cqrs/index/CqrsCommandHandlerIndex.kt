package com.github.xepozz.spiral.cqrs.index

import com.github.xepozz.spiral.SpiralFrameworkClasses
import com.github.xepozz.spiral.index.AbstractIndex
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

class CqrsCommandHandlerIndex : AbstractIndex<CommandHandlerType>() {
    companion object {
        val key = ID.create<String, CommandHandlerType>("Spiral.Cqrs.CommandHandler")
    }

    override fun getVersion() = 2

    override fun getName() = key

    override fun getValueExternalizer() = EnumeratorStringDescriptor.INSTANCE

    override fun getInputFilter() = FileBasedIndex.InputFilter { it.fileType == PhpFileType.INSTANCE }

    override fun getIndexer() = DataIndexer<String, CommandHandlerType, FileContent> { inputData ->
        inputData
            .psiFile
            .let { PsiTreeUtil.findChildrenOfType(it, PhpAttribute::class.java) }
            .find { it.fqn == SpiralFrameworkClasses.CQRS_COMMAND_HANDLER }
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