package com.github.xepozz.spiral.console.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.project.Project

class SpiralRunConfigurationFactory(private val runConfigurationType: SpiralConsoleCommandRunConfigurationType) :
    ConfigurationFactory(runConfigurationType) {
    override fun getId() = SpiralConsoleCommandRunConfigurationType.ID
    override fun getName() = runConfigurationType.displayName

    override fun createTemplateConfiguration(project: Project) =
        SpiralConsoleCommandRunConfiguration(project, this, "name")
}