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
import com.jetbrains.php.lang.psi.PhpPsiUtil
import com.jetbrains.php.lang.psi.elements.FieldReference
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.elements.PhpNamedElement
import com.jetbrains.php.lang.psi.elements.Variable
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider4
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeSignatureKey

class PrototypedTypeProvider : PhpTypeProvider4 {
    override fun getKey() = '\uA644'

    override fun getType(element: PsiElement): PhpType? {
        val project = element.project
        if (DumbService.isDumb(project)) return null

        val element = element as? FieldReference ?: return null
        if (!PhpLangUtil.isThisReference(element.classReference)) return null

        val currentPhpClass = PsiTreeUtil.getParentOfType(element, PhpClass::class.java) ?: return null
        if (!currentPhpClass.hasTrait(SpiralFrameworkClasses.PROTOTYPE_TRAIT)) return null

        val fieldName = element.name ?: return null

        val prototypes = PrototypedIndex.getPrototypes(project)

        if (fieldName !in prototypes) return null

        val prototypeFqn = PrototypedIndex.getPrototypeClass(fieldName, project) ?: return null

        val phpIndex = PhpIndex.getInstance(project)

        val classes = phpIndex.getAnyByFQN(prototypeFqn)

        val phpClass = classes.firstOrNull() ?: return null

//        println("prototype fieldName: $fieldName class: $phpClass")

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