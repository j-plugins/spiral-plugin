package com.github.xepozz.spiral.console.run

import com.github.xepozz.spiral.SpiralIcons
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationTypeBase
import com.intellij.openapi.project.Project

class SpiralConsoleCommandRunConfigurationType : ConfigurationTypeBase(
    ID,
    "Spiral Command",
    "Runs console command",
    SpiralIcons.SPIRAL,
) {
    init {
        addFactory(object : ConfigurationFactory(this) {
            override fun getId() = ID

            override fun createTemplateConfiguration(project: Project) =
                SpiralConsoleCommandRunConfiguration(project, this, "Spiral")

            override fun getOptionsClass() = SpiralConsoleCommandRunConfigurationSettings::class.java
        })
    }

    companion object {
        const val ID = "SpiralConsoleCommandRunConfiguration"

        val INSTANCE = SpiralConsoleCommandRunConfigurationType()
    }
}