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

typealias RouterIndexType = Route

abstract class AbstractRouterIndex : AbstractIndex<RouterIndexType>() {
    override fun getVersion() = 3

    override fun getValueExternalizer() = ObjectStreamDataExternalizer<RouterIndexType>()

    override fun getInputFilter() = FileBasedIndex.InputFilter {
        it.fileType == PhpFileType.INSTANCE && !it.name.endsWith(SpiralViewUtil.VIEW_SUFFIX)
    }

    protected fun parseRoutes(inputData: FileContent): List<Route> = inputData
        .psiFile
        .let { PsiTreeUtil.findChildrenOfType(it, PhpAttribute::class.java) }
        .filter { it.fqn == SpiralFrameworkClasses.ROUTE }
        .filter { it.arguments.isNotEmpty() }
        .mapNotNull { attribute ->
            val classMethod = attribute.owner as? Method ?: return@mapNotNull null

            val uri = RouterIndexUtil.parseContent(attribute.getPsiArgument("uri", 0))
            val name = RouterIndexUtil.parseContent(attribute.getPsiArgument("name", 1))
            val methods = RouterIndexUtil.parseMethods(attribute.getPsiArgument("methods", 2))
            val group = RouterIndexUtil.parseContent(attribute.getPsiArgument("group", 4))

            return@mapNotNull Route(
                uri = uri,
                name = name,
                methods = methods,
                fqn = classMethod.fqn,
                group = group,
            )
        }

}