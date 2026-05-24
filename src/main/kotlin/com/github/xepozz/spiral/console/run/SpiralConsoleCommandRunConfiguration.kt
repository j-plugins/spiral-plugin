package com.github.xepozz.spiral.console.run

import com.github.xepozz.spiral.SpiralBundle
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.RuntimeConfigurationError
import com.intellij.execution.configurations.RuntimeConfigurationException
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
        val commandName = settings.commandName
            ?.takeIf { it.isNotBlank() }
            ?: throw RuntimeConfigurationError(SpiralBundle.message("run.config.error.commandName.empty"))

        command.setScript("app.php", false)
        command.addArgument(commandName)

        command.importCommandLineSettings(settings.commandLineSettings, command.workingDirectory)
        command.addEnvs(envs)
    }

    @Throws(RuntimeConfigurationException::class)
    override fun checkConfiguration() {
        super.checkConfiguration()
        if (settings.commandName.isNullOrBlank()) {
            throw RuntimeConfigurationError(SpiralBundle.message("run.config.error.commandName.empty"))
        }
    }

    override fun getOptions(): SpiralConsoleCommandRunConfigurationSettings =
        super.getOptions() as? SpiralConsoleCommandRunConfigurationSettings
            ?: SpiralConsoleCommandRunConfigurationSettings()

    override fun getOptionsClass() = SpiralConsoleCommandRunConfigurationSettings::class.java

    override fun getConfigurationEditor() = SpiralConsoleCommandSettingsEditor(project)

    override fun createSettings() = SpiralConsoleCommandRunConfigurationSettings()
}
