package com.github.xepozz.spiral.router.index

import com.github.xepozz.spiral.SpiralFrameworkClasses
import com.github.xepozz.spiral.SpiralViewUtil
import com.github.xepozz.spiral.common.index.ObjectStreamDataExternalizer
import com.github.xepozz.spiral.common.references.AttributesUtil.getPsiArgument
import com.github.xepozz.spiral.index.AbstractIndex
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ID
import com.jetbrains.php.lang.PhpFileType
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression
import com.jetbrains.php.lang.psi.elements.Method
import com.jetbrains.php.lang.psi.elements.PhpAttribute
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression

class RouterNamesIndex : AbstractRouterIndex() {
    companion object {
        val key = ID.create<String, RouterIndexType>("Spiral.Router.Names")
    }

    override fun getName() = key

    override fun getIndexer() = DataIndexer<String, RouterIndexType, FileContent> { inputData ->
        parseRoutes(inputData)
            .associateBy { it.name }
//            .apply { println("file: ${inputData.file}, result: $this") }
    }
}