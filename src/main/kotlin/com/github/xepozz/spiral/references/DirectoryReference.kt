package com.github.xepozz.spiral.references

import com.github.xepozz.spiral.SpiralIcons
import com.github.xepozz.spiral.SpiralViewUtil
import com.github.xepozz.spiral.php.contentRange
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.impl.PsiManagerEx
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression
import kotlin.io.path.Path

class DirectoryReference(
    val directory: String,
    element: StringLiteralExpression,
) : PsiReferenceBase<PsiElement>(element) {
    override fun resolve(): PsiElement? {
        val project = element.project
        val projectDir = project.guessProjectDir() ?: return null
        val offset = SpiralViewUtil.PREDEFINED_DIRS[directory]?.trim('/') ?: return null

        val path = Path("${projectDir.path}/$offset")

        val vf = VirtualFileManager.getInstance().findFileByNioPath(path) ?: return null

        return PsiManagerEx.getInstanceEx(project).findDirectory(vf)
    }

    override fun isSoft() = true

    override fun getVariants() = SpiralViewUtil
        .PREDEFINED_DIRS
        .map {
            LookupElementBuilder.create(it.key)
                .withIcon(SpiralIcons.SPIRAL)
                .withTypeText(it.value)
        }
        .toTypedArray()

    override fun calculateDefaultRangeInElement(): TextRange {
        val element = element as StringLiteralExpression

        return element.contentRange.shiftLeft(element.textOffset)
    }
}