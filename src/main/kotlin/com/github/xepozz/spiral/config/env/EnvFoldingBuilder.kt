package com.github.xepozz.spiral.config.env

import com.github.xepozz.spiral.SpiralFrameworkClasses
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.psi.elements.FunctionReference
import com.jetbrains.php.lang.psi.elements.MethodReference
import com.jetbrains.php.lang.psi.elements.Variable
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeSignatureKey

class EnvFoldingBuilder : FoldingBuilderEx() {
    companion object {
        val environmentSignature = PhpTypeSignatureKey.CLASS.sign(SpiralFrameworkClasses.ENVIRONMENT_INTERFACE)
    }

    override fun buildFoldRegions(
        root: PsiElement,
        document: Document,
        quick: Boolean
    ): Array<out FoldingDescriptor?> = PsiTreeUtil.findChildrenOfType(root, FunctionReference::class.java)
        .mapNotNull { call ->
            if (call is MethodReference) {
                if (call.name != "get") return@mapNotNull null

                val variable = call.classReference as? Variable ?: return@mapNotNull null
                if (variable.signature != environmentSignature) return@mapNotNull null
            } else {
                if (call.fqn != SpiralFrameworkClasses.ENV_FUNCTION) return@mapNotNull null
            }
            val firstArg = call.parameters.getOrNull(0) ?: return@mapNotNull null
            val foldingDescriptor = FoldingDescriptor(call, call.textRange)
            foldingDescriptor.placeholderText = "env: ${firstArg.text}"
            foldingDescriptor
        }
        .toTypedArray()

    override fun isCollapsedByDefault(node: ASTNode) = true

    override fun getPlaceholderText(node: ASTNode) = null
}