package com.github.xepozz.spiral.console.run

import com.github.xepozz.spiral.SpiralBundle
import com.github.xepozz.spiral.SpiralIcons
import com.intellij.execution.configurations.ConfigurationTypeBase

class SpiralConsoleCommandRunConfigurationType : ConfigurationTypeBase(
    ID,
    SpiralBundle.message("configuration.type.name"),
    SpiralBundle.message("configuration.type.description"),
    SpiralIcons.SPIRAL,
) {
    init {
        addFactory(SpiralRunConfigurationFactory(this))
    }

    companion object {
        /**
         * IMPORTANT: This ID is persisted in users' workspace.xml as the configuration
         * type identifier. Changing it will break every saved Spiral run configuration
         * across all installations. DO NOT MODIFY.
         */
        const val ID = "SpiralConsoleCommandRunConfiguration"

        val INSTANCE = SpiralConsoleCommandRunConfigurationType()
    }
}
