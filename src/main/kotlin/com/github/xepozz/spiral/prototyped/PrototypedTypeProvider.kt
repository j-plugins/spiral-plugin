package com.github.xepozz.spiral.prototyped

import com.github.xepozz.spiral.SpiralFrameworkClasses
import com.github.xepozz.spiral.config.index.PrototypedIndex
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.PhpIndex
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
        val variable = element.classReference as? Variable ?: return null
        if (variable.name != "this") return null

        val phpClass = PsiTreeUtil.getParentOfType(element, PhpClass::class.java) ?: return null
        val traits = phpClass.traits
        if (traits.isEmpty()) return null
        if (traits.none { it.fqn == SpiralFrameworkClasses.PROTOTYPE_TRAIT }) return null

        val fieldName = element.name ?: return null

        val prototypes = PrototypedIndex.getPrototypes(project)

        if (fieldName !in prototypes) return null

//        println("prototypes: ${prototypes}, fieldName: $fieldName")
        return getBySignature(fieldName, emptySet(), 0, project)?.firstOrNull()?.type
//        return PhpType().add("#${key}$fieldName")
    }

    override fun complete(
        signature: String,
        project: Project,
    ): PhpType? {
//        println("complete: $signature")
        val prototypeName = signature.substringAfter(key)

        return getBySignature(prototypeName, emptySet(), 0, project)?.firstOrNull()?.type
    }

    override fun getBySignature(
        expression: String,
        visited: Set<String>,
        depth: Int,
        project: Project,
    ): Collection<PhpNamedElement>? {
//        println("getBySignature: $expression")

        val prototypeFqn = PrototypedIndex.getPrototypeClass(expression, project)
        val phpIndex = PhpIndex.getInstance(project)
        val classes = phpIndex.getAnyByFQN(prototypeFqn)

//        println("find classes $classes by prototypeFqn $prototypeFqn")
        if (classes.isEmpty()) return null

        return classes
    }
}