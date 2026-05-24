package com.github.xepozz.spiral.prototyped

import com.github.xepozz.spiral.config.index.PrototypedIndex
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase

/**
 * Reference to a prototype property name.
 *
 * Note: [resolve] intentionally returns null. The actual resolution of `$this->propertyName`
 * to its underlying class is delegated to PhpStorm's built-in field resolution via the
 * type returned by [PrototypedTypeProvider] (which contributes the prototype class's FQN
 * as the type of the field reference). See `PrototypedTypeProvider.getType`.
 *
 * This class is currently not wired into any reference contributor and exists as a placeholder
 * for the rare case where a custom resolution target is required (e.g. navigating directly
 * to the `#[Prototyped]` attribute declaration).
 */
class PrototypedPropertyReference(
    val property: String,
    val range: TextRange,
    element: PsiElement,
) : PsiReferenceBase<PsiElement>(element) {
    override fun resolve(): PsiElement? = null

    override fun isSoft() = true

    override fun getRangeInElement() = range

    override fun getVariants(): Array<out Any?> {
        val project = element.project
        if (DumbService.isDumb(project)) return emptyArray()

        return PrototypedIndex.getPrototypes(project).toTypedArray()
    }
}
