package com.github.xepozz.spiral.console.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.RunConfigurationOptions
import com.intellij.openapi.project.Project
import com.jetbrains.php.config.commandLine.PhpCommandSettings
import com.jetbrains.php.run.PhpCommandLineRunConfiguration

class SpiralConsoleCommandRunConfiguration(
    project: Project,
    factory: ConfigurationFactory,
    name: String
) : PhpCommandLineRunConfiguration<SpiralConsoleCommandRunConfigurationSettings>(project, factory, name) {
    override fun fillCommandSettings(
        envs: Map<String, String>,
        command: PhpCommandSettings
    ) {
        val commandName = settings.commandName ?: return
        command.setScript("app.php", false)
        command.addArgument(commandName)

        command.importCommandLineSettings(settings.commandLineSettings, command.workingDirectory)
        command.addEnvs(envs)
    }

    override fun getOptions() = super.getOptions() as SpiralConsoleCommandRunConfigurationSettings
    override fun getOptionsClass(): Class<out RunConfigurationOptions> {
        return SpiralConsoleCommandRunConfigurationSettings::class.java
    }

    override fun getConfigurationEditor() = SpiralConsoleCommandSettingsEditor(project)

    override fun createSettings() = SpiralConsoleCommandRunConfigurationSettings().apply {
    }
}