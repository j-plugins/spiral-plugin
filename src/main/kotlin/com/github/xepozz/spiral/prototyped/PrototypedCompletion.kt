package com.github.xepozz.spiral.prototyped

import com.github.xepozz.spiral.SpiralFrameworkClasses
import com.github.xepozz.spiral.SpiralIcons
import com.github.xepozz.spiral.config.index.PrototypedIndex
import com.github.xepozz.spiral.php.hasTrait
import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbService
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.jetbrains.php.lang.psi.elements.FieldReference
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.elements.Variable

class PrototypedCompletion : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .withParent(
                    PlatformPatterns.psiElement(FieldReference::class.java)
                        .withFirstChild(
                            PlatformPatterns.psiElement(Variable::class.java)
                                .withName("this")
                        )
                ),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    val element = parameters.position.parent as? FieldReference ?: return
                    val project = element.project
                    if (DumbService.isDumb(project)) return

                    val phpClass = PsiTreeUtil.getParentOfType(element, PhpClass::class.java) ?: return
                    if (!phpClass.hasTrait(SpiralFrameworkClasses.PROTOTYPE_TRAIT)) return

                    for ((name, fqn) in PrototypedIndex.getAll(project)) {
                        ProgressManager.checkCanceled()
                        if (result.isStopped) return
                        result.addElement(
                            LookupElementBuilder.create(name)
                                .withIcon(SpiralIcons.SPIRAL)
                                .withTypeText(fqn)
                        )
                    }
                }
            }
        )
    }
}
