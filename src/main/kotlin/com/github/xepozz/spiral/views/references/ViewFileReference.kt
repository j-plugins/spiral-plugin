package com.github.xepozz.spiral.views.references

import com.github.xepozz.spiral.SpiralViewUtil
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.impl.PsiManagerEx
import kotlin.io.path.Path

class ViewFileReference(
    val name: String,
    val range: TextRange,
    element: PsiElement,
) : PsiReferenceBase<PsiElement>(element) {
    override fun resolve(): PsiElement? {
        val project = element.project
        val projectDir = project.guessProjectDir() ?: return null
        val offset = SpiralViewUtil.PREDEFINED_DIRS["views"]?.trim('/') ?: return null

        val path = Path("${projectDir.path}/$offset/$name.${SpiralViewUtil.VIEW_SUFFIX}")

        val vf = VirtualFileManager.getInstance().findFileByNioPath(path) ?: return null

        return PsiManagerEx.getInstanceEx(project).findFile(vf)
    }

    override fun isSoft() = true
    override fun getRangeInElement() = range
}