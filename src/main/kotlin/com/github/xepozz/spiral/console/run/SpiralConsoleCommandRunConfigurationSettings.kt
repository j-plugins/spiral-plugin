package com.github.xepozz.spiral.console.run

import com.intellij.execution.configurations.LocatableRunConfigurationOptions
import com.jetbrains.php.run.PhpCommandLineSettings
import com.jetbrains.php.run.PhpRunConfigurationSettings

/**
 * Persistent settings for a Spiral console command run configuration.
 *
 * The second argument of `string("...").provideDelegate(this, "<name>")` is the XML
 * attribute name used to persist the value to `workspace.xml`. These names MUST stay
 * stable to preserve users' saved configurations.
 */
class SpiralConsoleCommandRunConfigurationSettings : PhpRunConfigurationSettings, LocatableRunConfigurationOptions() {
    private val myCommandName = string("").provideDelegate(this, "commandName")
    private val myBinary = string("./app.php").provideDelegate(this, "binary")
    private val myWorkingDirectory = string("").provideDelegate(this, "workingDirectory")
    private val myDocumentRoot = string("").provideDelegate(this, "documentRoot")

    /** The Spiral console command name, e.g. `migrate:init`. */
    var commandName: String?
        get() = myCommandName.getValue(this)
        set(value) {
            myCommandName.setValue(this, value)
        }

    /** Path to the PHP script binary, defaults to `./app.php`. */
    var binary: String?
        get() = myBinary.getValue(this)
        set(value) {
            myBinary.setValue(this, value)
        }

    /** Document root passed through to the underlying PHP command-line settings. */
    var documentRoot: String?
        get() = myDocumentRoot.getValue(this)
        set(value) {
            myDocumentRoot.setValue(this, value)
        }

    /** Delegated PHP command-line settings (interpreter, env vars, etc.). */
    val commandLineSettings = PhpCommandLineSettings()

    override fun getWorkingDirectory() = myWorkingDirectory.getValue(this)

    override fun setWorkingDirectory(workingDirectory: String?) {
        myWorkingDirectory.setValue(this, workingDirectory)
    }
}
