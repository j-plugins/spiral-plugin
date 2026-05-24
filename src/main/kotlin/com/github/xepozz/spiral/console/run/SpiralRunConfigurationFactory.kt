package com.github.xepozz.spiral.console.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.project.Project

class SpiralRunConfigurationFactory(private val runConfigurationType: SpiralConsoleCommandRunConfigurationType) :
    ConfigurationFactory(runConfigurationType) {

    /**
     * Stable across versions — DO NOT MODIFY. The returned ID is persisted in users'
     * `workspace.xml`; changing it would orphan every saved Spiral run configuration.
     */
    override fun getId(): String = SpiralConsoleCommandRunConfigurationType.ID

    override fun getName(): String = runConfigurationType.displayName

    override fun createTemplateConfiguration(project: Project) =
        SpiralConsoleCommandRunConfiguration(project, this, runConfigurationType.displayName)

    override fun getOptionsClass() = SpiralConsoleCommandRunConfigurationSettings::class.java
}
