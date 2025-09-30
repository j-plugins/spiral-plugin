package com.github.xepozz.spiral.references

import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.impl.PsiManagerEx
import kotlin.io.path.Path

class DirectoryReference(
    val directory: String,
    val range: TextRange,
    element: PsiElement,
) : PsiReferenceBase<PsiElement>(element) {
    override fun resolve(): PsiElement? {
        val project = element.project
        val projectDir = project.guessProjectDir() ?: return null
        val offset = PREDEFINED_DIRS[directory]?.trim('/') ?: return null

        val path = Path("${projectDir.path}/$offset")

        val vf = VirtualFileManager.getInstance().findFileByNioPath(path) ?: return null

        return PsiManagerEx.getInstanceEx(project).findDirectory(vf)
    }

    override fun isSoft() = true
    override fun getRangeInElement() = range

    companion object {
        val PREDEFINED_DIRS = mapOf(
            "public" to "/public/",
            "vendor" to "/vendor/",
            "runtime" to "/runtime/",
            "cache" to "/runtime/cache/",
            "config" to "/app/config/",
            "resources" to "/app/resources/",
            "root" to "/",
            "app" to "/app/",
        )
    }
}