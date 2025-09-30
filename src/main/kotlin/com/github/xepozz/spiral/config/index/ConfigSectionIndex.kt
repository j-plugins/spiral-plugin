package com.github.xepozz.spiral.config.index

import com.github.xepozz.spiral.index.AbstractIndex
import com.github.xepozz.spiral.SpiralFrameworkClasses
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ID
import com.intellij.util.io.EnumeratorStringDescriptor
import com.jetbrains.php.lang.PhpFileType
import com.jetbrains.php.lang.psi.elements.PhpClass

private typealias ConfigSectionIndexType = String

class ConfigSectionIndex : AbstractIndex<ConfigSectionIndexType>() {
    companion object {
        val key = ID.create<String, ConfigSectionIndexType>("Spiral.ConfigSection")
    }

    override fun getVersion() = 1

    override fun getName() = key

    override fun getValueExternalizer() = EnumeratorStringDescriptor.INSTANCE

    override fun getInputFilter() = FileBasedIndex.InputFilter { it.fileType == PhpFileType.INSTANCE }

    override fun getIndexer() = DataIndexer<String, ConfigSectionIndexType, FileContent> { inputData ->
        return@DataIndexer inputData
            .psiFile
            .let { PsiTreeUtil.findChildrenOfType(it, PhpClass::class.java) }
            .filter { it.superFQN == SpiralFrameworkClasses.INJECTABLE_CONFIG }
            .map { it.fqn }
            .associateBy { it }
    }
}