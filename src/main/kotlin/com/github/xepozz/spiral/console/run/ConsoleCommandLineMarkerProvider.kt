package com.github.xepozz.spiral.console.run

import com.github.xepozz.spiral.SpiralBundle
import com.github.xepozz.spiral.php.getConsoleCommandName
import com.intellij.execution.lineMarker.ExecutorAction
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.elements.Method
import com.jetbrains.php.lang.psi.elements.PhpClass

class ConsoleCommandLineMarkerProvider : RunLineMarkerContributor() {
    override fun getInfo(element: PsiElement) = when {
        element !is PhpClass -> null
        else -> {
            val commandName = element.getConsoleCommandName() ?: return null
            Info(
                AllIcons.Actions.Execute,
                ExecutorAction.getActions(1),
            ) {
                SpiralBundle.message(
                    "action.run.target.text",
                    StringUtil.wrapWithDoubleQuote(SpiralBundle.message("action.run.target.command", commandName)),
                )
            }
        }
    }
}