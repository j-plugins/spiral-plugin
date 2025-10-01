package com.github.xepozz.spiral.views.index

import com.github.xepozz.spiral.index.AbstractIndex
import com.github.xepozz.spiral.SpiralFrameworkClasses
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ID
import com.intellij.util.io.EnumeratorStringDescriptor
import com.jetbrains.php.PhpWorkaroundUtil
import com.jetbrains.php.lang.PhpFileType
import com.jetbrains.php.lang.PhpLangUtil
import com.jetbrains.php.lang.psi.elements.BinaryExpression
import com.jetbrains.php.lang.psi.elements.ConcatenationExpression
import com.jetbrains.php.lang.psi.elements.ConstantReference
import com.jetbrains.php.lang.psi.elements.MethodReference
import com.jetbrains.php.lang.psi.elements.ParameterList
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression
import com.jetbrains.php.lang.psi.elements.Variable
import com.jetbrains.php.lang.psi.elements.impl.PhpFilePathUtils
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeSignatureKey

private typealias ViewsNamespaceIndexType = String

class ViewsNamespaceIndex : AbstractIndex<ViewsNamespaceIndexType>() {
    companion object {
        val key = ID.create<String, ViewsNamespaceIndexType>("Spiral.ViewsNamespace")
    }

//    override fun getVersion() = 3

    override fun getName() = key

    override fun getValueExternalizer() = EnumeratorStringDescriptor.INSTANCE

    override fun getInputFilter() = FileBasedIndex.InputFilter { it.fileType == PhpFileType.INSTANCE }

    override fun getIndexer() = DataIndexer<String, ViewsNamespaceIndexType, FileContent> { inputData ->
        return@DataIndexer inputData
            .psiFile
            .let { PsiTreeUtil.findChildrenOfType(it, MethodReference::class.java) }
            .filter {
                it.name == "addDirectory"
                        && (it.classReference as? Variable)?.signature == PhpTypeSignatureKey.CLASS.sign(
                    SpiralFrameworkClasses.VIEWS_BOOTLOADER
                )
            }
            .mapNotNull {
                if (it.parameters.size < 2) return@mapNotNull null
                val key = getTextValue(it.parameters[0])
                val value = getTextValue(it.parameters[1])

                return@mapNotNull key to value
            }
            .toMap()
    }

    override fun getVersion() = 3

    private fun getTextValue(element: PsiElement): String? = when (element) {
        is ConstantReference -> when {
            element.text == "__DIR__" -> element.containingFile.virtualFile.parent.path
            else -> element.text
        }
        is StringLiteralExpression -> element.contents
        is BinaryExpression -> element.children.toList().mapNotNull { getTextValue(it) }.joinToString("")
        else -> element.text
    }
}