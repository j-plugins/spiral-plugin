package com.github.xepozz.spiral.console.run

import com.github.xepozz.spiral.SpiralBundle
import com.github.xepozz.spiral.php.getConsoleCommandName
import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.elements.Method
import com.jetbrains.php.lang.psi.elements.PhpClass

class SpiralRunConfigurationProducer : LazyRunConfigurationProducer<SpiralConsoleCommandRunConfiguration>() {
    override fun setupConfigurationFromContext(
        configuration: SpiralConsoleCommandRunConfiguration,
        context: ConfigurationContext,
        sourceElement: Ref<PsiElement>
    ): Boolean {
        val element = context.psiLocation as? PhpClass ?: return false
        val commandName = element.getConsoleCommandName() ?: return false

        configuration.settings.commandName = commandName
        configuration.name = SpiralBundle.message("action.run.target.command", commandName)

        return true
    }

    override fun isConfigurationFromContext(
        configuration: SpiralConsoleCommandRunConfiguration,
        context: ConfigurationContext
    ): Boolean {
        val method = context.psiLocation as? PhpClass ?: return false

        return configuration.settings.commandName == method.getConsoleCommandName()
    }

    override fun getConfigurationFactory() =
        SpiralRunConfigurationFactory(SpiralConsoleCommandRunConfigurationType.INSTANCE)
}
