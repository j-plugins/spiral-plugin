package com.github.xepozz.spiral.console.run

import com.github.xepozz.spiral.SpiralBundle
import com.github.xepozz.spiral.php.getConsoleCommandName
import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.psi.elements.PhpClass

class SpiralRunConfigurationProducer : LazyRunConfigurationProducer<SpiralConsoleCommandRunConfiguration>() {
    override fun setupConfigurationFromContext(
        configuration: SpiralConsoleCommandRunConfiguration,
        context: ConfigurationContext,
        sourceElement: Ref<PsiElement>
    ): Boolean {
        val phpClass = findCommandClass(context) ?: return false
        val commandName = phpClass.getConsoleCommandName() ?: return false

        configuration.settings.commandName = commandName
        configuration.name = SpiralBundle.message("action.run.target.command", commandName)

        return true
    }

    override fun isConfigurationFromContext(
        configuration: SpiralConsoleCommandRunConfiguration,
        context: ConfigurationContext
    ): Boolean {
        val phpClass = findCommandClass(context) ?: return false

        return configuration.settings.commandName == phpClass.getConsoleCommandName()
    }

    override fun getConfigurationFactory(): ConfigurationFactory =
        SpiralConsoleCommandRunConfigurationType.INSTANCE.configurationFactories.first()

    /**
     * Resolves the [PhpClass] that owns the context's PSI location. The user may have
     * the caret on a leaf token within the class declaration, so we walk up the tree
     * rather than restricting to direct class hits.
     */
    private fun findCommandClass(context: ConfigurationContext): PhpClass? {
        val location = context.psiLocation ?: return null
        return location as? PhpClass
            ?: PsiTreeUtil.getParentOfType(location, PhpClass::class.java)
    }
}
