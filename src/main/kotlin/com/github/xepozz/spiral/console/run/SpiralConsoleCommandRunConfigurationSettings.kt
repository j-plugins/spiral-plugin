package com.github.xepozz.spiral.console.run

import com.intellij.execution.configurations.LocatableRunConfigurationOptions
import com.jetbrains.php.run.PhpCommandLineSettings
import com.jetbrains.php.run.PhpRunConfigurationSettings

class SpiralConsoleCommandRunConfigurationSettings : PhpRunConfigurationSettings, LocatableRunConfigurationOptions() {
    private val myCommandName = string("").provideDelegate(this, "commandName")
    private val myBinary = string("./app.php").provideDelegate(this, "binary")
    private val myDocumentRoot = string("").provideDelegate(this, "documentRoot")
    private val myWorkingDirectory = string("").provideDelegate(this, "workingDirectory")

    var commandName: String?
        get() = myCommandName.getValue(this)
        set(scriptName) {
            myCommandName.setValue(this, scriptName)
        }

    var binary: String?
        get() = myBinary.getValue(this)
        set(scriptName) {
            myBinary.setValue(this, scriptName)
        }

    var documentRoot: String?
        get() = myDocumentRoot.getValue(this)
        set(value) {
            myDocumentRoot.setValue(this, value)
        }

    var commandLineSettings = PhpCommandLineSettings()

    override fun getWorkingDirectory() = myWorkingDirectory.getValue(this)

    override fun setWorkingDirectory(p0: String?) {
        myWorkingDirectory.setValue(this, p0)
    }
}