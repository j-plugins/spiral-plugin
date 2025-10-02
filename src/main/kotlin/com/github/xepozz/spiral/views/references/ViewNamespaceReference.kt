package com.github.xepozz.spiral.views.references

import com.github.xepozz.spiral.SpiralIcons
import com.github.xepozz.spiral.common.references.InsertTextInsertHandler
import com.github.xepozz.spiral.views.index.ViewsNamespaceIndexUtil
import com.intellij.codeInsight.completion.DeclarativeInsertHandler.PopupOptions.DoNotShow
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.impl.PsiManagerEx
import com.intellij.psi.impl.source.tree.injected.changesHandler.contentRange
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression
import kotlin.io.path.Path

class ViewNamespaceReference(
    val name: String,
    element: StringLiteralExpression,
) : PsiReferenceBase<PsiElement>(element) {
    override fun resolve(): PsiElement? {
        val project = element.project
        if (!name.contains(':')) return null
        val namespaceName = name.substringBefore(':')

        val namespacePath = ViewsNamespaceIndexUtil.getNamespace(namespaceName, project) ?: return null

//        println("namespacePath: $namespacePath")

        val path = Path(namespacePath)

        val vf = VirtualFileManager.getInstance().findFileByNioPath(path) ?: return null

        return PsiManagerEx.getInstanceEx(project).findDirectory(vf)
    }

    override fun getVariants(): Array<out Any?> {
        val project = element.project
        val projectDir = project.guessProjectDir() ?: return emptyArray()

//        println("lookup for namespace: $rangeInElement")
        return ViewsNamespaceIndexUtil
            .getAllNamespaces(project)
            .map {
                LookupElementBuilder.create(it.key)
                    .withIcon(SpiralIcons.SPIRAL)
                    .withTailText(" " + it.value.removePrefix(projectDir.path).removePrefix("/"))
                    .withInsertHandler(InsertTextInsertHandler(":", DoNotShow))
                    .withTypeText("Namespace")
            }
            .toTypedArray()
    }

    override fun isSoft() = true

    override fun calculateDefaultRangeInElement(): TextRange {
        val element = element as StringLiteralExpression
        val delimiterIndex = element.text.indexOf(':')

        return when (delimiterIndex) {
            -1 -> element.contentRange.shiftLeft(element.textOffset)
            else -> TextRange(1, delimiterIndex)
        }
            .apply { println("range: $this of element: ${element}, delimiterIndex: $delimiterIndex") }
    }
}


