package com.github.xepozz.spiral.views.references

import com.github.xepozz.spiral.SpiralViewUtil
import com.github.xepozz.spiral.views.index.ViewsNamespaceIndexUtil
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

class ViewFileReference(
    val name: String,
    element: StringLiteralExpression,
) : PsiReferenceBase<PsiElement>(element) {
    override fun resolve(): PsiElement? {
        val project = element.project
        val projectDir = project.guessProjectDir() ?: return null

        var basePath: String? = null
        if (name.indexOf(':') > 0) {
            basePath = ViewsNamespaceIndexUtil.getNamespace(name.substringBefore(':'), project) ?: return null
        } else {
            val offset = SpiralViewUtil.PREDEFINED_DIRS["views"]?.trim('/') ?: return null
            basePath = "${projectDir.path}/$offset"
        }
        val filename = name.substringAfter(':')

        val path = Path("$basePath/$filename$fileSuffix")

        val vf = VirtualFileManager.getInstance().findFileByNioPath(path) ?: return null

        return PsiManagerEx.getInstanceEx(project).findFile(vf)
    }

    override fun getVariants(): Array<out Any?> {
        val project = element.project
        val projectDir = project.guessProjectDir() ?: return emptyArray()

        var basePath: String?
        if (name.indexOf(':') > 0) {
            basePath = ViewsNamespaceIndexUtil.getNamespace(name.substringBefore(':'), project) ?: return emptyArray()
        } else {
            val offset = SpiralViewUtil.PREDEFINED_DIRS["views"]?.trim('/') ?: return emptyArray()
            basePath = "${projectDir.path}/$offset"
        }

        val path = Path(basePath)

        val vf = VirtualFileManager.getInstance().findFileByNioPath(path) ?: return emptyArray()

        val psiDirectory = PsiManagerEx.getInstanceEx(project).findDirectory(vf) ?: return emptyArray()

        return psiDirectory
            .files
            .filter { it.name.endsWith(fileSuffix) }
            .map {
                LookupElementBuilder.create(it)
                    .withLookupString(it.name.substringBefore(fileSuffix))
                    .withBaseLookupString(it.name.substringBefore(fileSuffix))
                    .withIcon(it.getIcon(0))
                    .withTailText(fileSuffix)
                    .withPsiElement(it)
            }
            .toTypedArray()
    }

    override fun isSoft() = true
    override fun calculateDefaultRangeInElement(): TextRange? {
        val element = element as StringLiteralExpression
        val delimiterIndex = element.text.indexOf(':')

        return when (delimiterIndex) {
            -1 -> element.contentRange.shiftLeft(element.textOffset)
            else -> TextRange(element.text.indexOf(':') + 1, element.text.length - 1)
        }
    }

    companion object{
        const val fileSuffix = ".${SpiralViewUtil.VIEW_SUFFIX}"
    }
}