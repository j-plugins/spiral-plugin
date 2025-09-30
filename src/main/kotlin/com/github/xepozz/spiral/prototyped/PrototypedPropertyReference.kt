package com.github.xepozz.spiral.prototyped

import com.github.xepozz.spiral.config.index.PrototypedIndex
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.jetbrains.php.PhpIndex

class PrototypedPropertyReference(
    val property: String,
    val range: TextRange,
    element: PsiElement,
) : PsiReferenceBase<PsiElement>(element) {
    override fun resolve(): PsiElement? {
        val project = element.project

        return null
//        val prototypeFqn = PrototypedIndex.getPrototypeClass(expression, project)
//        val phpIndex = PhpIndex.getInstance(project)
//        val classes = phpIndex.getAnyByFQN(prototypeFqn)

//        return PsiManagerEx.getInstanceEx(project).findDirectory(vf)
    }

    override fun isSoft() = true
    override fun getRangeInElement() = range

    override fun getVariants(): Array<out Any?> {
        val properties = PrototypedIndex.getPrototypes(element.project)
        val phpIndex = PhpIndex.getInstance(element.project)

        return properties.toTypedArray()
    }

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