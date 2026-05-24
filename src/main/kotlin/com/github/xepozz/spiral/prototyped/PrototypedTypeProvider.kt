package com.github.xepozz.spiral.prototyped

import com.github.xepozz.spiral.SpiralFrameworkClasses
import com.github.xepozz.spiral.config.index.PrototypedIndex
import com.github.xepozz.spiral.php.hasTrait
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.PhpLangUtil
import com.jetbrains.php.lang.psi.elements.FieldReference
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider4

class PrototypedTypeProvider : PhpTypeProvider4 {
    /**
     * PhpStorm uses this character as a per-provider registry key in cached type signatures.
     * Must be unique across all `PhpTypeProvider4` implementations within the IDE process.
     * The chosen Unicode character (Cyrillic capital letter Reversed Tse, U+A644) is intentionally
     * obscure to avoid collisions with platform/JetBrains-bundled type providers.
     *
     * Do NOT change this value — it is persisted in PhpStorm's type signature cache and changing
     * it would invalidate existing caches.
     */
    override fun getKey() = 'Ꙅ'

    override fun getType(element: PsiElement): PhpType? {
        val project = element.project
        if (DumbService.isDumb(project)) return null

        val fieldRef = element as? FieldReference ?: return null
        if (!PhpLangUtil.isThisReference(fieldRef.classReference)) return null

        val currentPhpClass = PsiTreeUtil.getParentOfType(fieldRef, PhpClass::class.java) ?: return null
        if (!currentPhpClass.hasTrait(SpiralFrameworkClasses.PROTOTYPE_TRAIT)) return null

        val fieldName = fieldRef.name ?: return null

        val prototypes = PrototypedIndex.getPrototypes(project)
        if (fieldName !in prototypes) return null

        val prototypeFqn = PrototypedIndex.getPrototypeClass(fieldName, project) ?: return null

        val phpIndex = PhpIndex.getInstance(project)
        val phpClass = phpIndex.getAnyByFQN(prototypeFqn).firstOrNull() ?: return null

        return PhpType().add(phpClass.fqn)
    }

    override fun complete(
        signature: String,
        project: Project,
    ) = null

    override fun getBySignature(
        expression: String,
        visited: Set<String>,
        depth: Int,
        project: Project,
    ) = null
}
