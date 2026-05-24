package com.github.xepozz.spiral.console.run

import com.github.xepozz.spiral.SpiralBundle
import com.github.xepozz.spiral.php.getConsoleCommandName
import com.intellij.execution.lineMarker.ExecutorAction
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.psi.elements.PhpClass

class ConsoleCommandLineMarkerProvider : RunLineMarkerContributor() {
    override fun getInfo(element: PsiElement): Info? {
        // RunLineMarkerContributor#getInfo is invoked on leaf elements; only the leaf
        // identifier of the class declaration should carry the gutter icon, otherwise
        // the icon would appear repeatedly on every descendant element.
        if (element.firstChild != null) return null

        val phpClass = PsiTreeUtil.getParentOfType(element, PhpClass::class.java) ?: return null
        if (phpClass.nameIdentifier !== element) return null

        // The line marker may be queried during indexing; bail out instead of touching
        // PHP attribute machinery in dumb mode.
        if (DumbService.isDumb(element.project)) return null

        val commandName = phpClass.getConsoleCommandName() ?: return null
        return Info(
            AllIcons.Actions.Execute,
            ExecutorAction.getActions(1),
        ) {
            SpiralBundle.message("action.run.target.text", commandName)
        }
    }
}
