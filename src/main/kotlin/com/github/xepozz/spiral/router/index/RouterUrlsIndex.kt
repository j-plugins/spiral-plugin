package com.github.xepozz.spiral.router.index

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
import com.jetbrains.php.lang.psi.elements.Method
import com.jetbrains.php.lang.psi.elements.PhpAttribute
import com.jetbrains.php.lang.psi.elements.PhpNamedElement

private typealias RouterUrlsIndexType = String

/**
 * Stores uri -> controller/action association
 */
class RouterUrlsIndex : AbstractIndex<RouterUrlsIndexType>() {
    companion object {
        val key = ID.create<String, RouterUrlsIndexType>("Spiral.Router.Urls")
    }

    override fun getVersion() = 1

    override fun getName() = key

    override fun getValueExternalizer() = EnumeratorStringDescriptor.INSTANCE

    override fun getInputFilter() = FileBasedIndex.InputFilter {
        it.fileType == PhpFileType.INSTANCE && !it.name.endsWith(SpiralViewUtil.VIEW_SUFFIX)
    }

    override fun getIndexer() = DataIndexer<String, RouterUrlsIndexType, FileContent> { inputData ->
        inputData
            .psiFile
            .let { PsiTreeUtil.findChildrenOfType(it, PhpAttribute::class.java) }
            .filter { it.fqn == SpiralFrameworkClasses.ROUTE }
            .filter { it.arguments.isNotEmpty() }
            .mapNotNull { attribute ->
                attribute.owner to attribute.arguments
                    .firstOrNull { it.name == "route" }
                    .let { it ?: attribute.arguments.getOrNull(0) }
                    ?.argument
                    ?.value
                    ?.let { StringUtil.unquoteString(it) }
            }
            .filter { it.first is Method }
            .filter { !it.second.isNullOrEmpty() }
            .associate { it.second to (it.first as PhpNamedElement).fqn }
//            .apply { println("file: ${inputData.file}, result: $this") }
    }
}