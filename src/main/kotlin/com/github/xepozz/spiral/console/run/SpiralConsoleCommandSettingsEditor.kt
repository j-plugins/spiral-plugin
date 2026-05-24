package com.github.xepozz.spiral.console.run

import com.github.xepozz.spiral.SpiralBundle
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.LabelPosition
import com.intellij.ui.dsl.builder.TopGap
import com.intellij.ui.dsl.builder.panel
import com.jetbrains.php.run.PhpCommandLineConfigurationEditor
import javax.swing.JPanel
import javax.swing.JTextField

private val LOG = Logger.getInstance(SpiralConsoleCommandSettingsEditor::class.java)

/**
 * Reflectively reaches into PhpStorm's `PhpCommandLineConfigurationEditor` to extract
 * its main panel — there is no public API for that. Wrapped in try/catch so an IDE
 * update that renames the field degrades gracefully instead of breaking the editor.
 */
private fun PhpCommandLineConfigurationEditor.getMainPanel(): JPanel? = try {
    val field = PhpCommandLineConfigurationEditor::class.java.getDeclaredField("myMainPanel")
    field.isAccessible = true
    field.get(this) as? JPanel
} catch (e: ReflectiveOperationException) {
    LOG.warn("Failed to access PhpCommandLineConfigurationEditor#myMainPanel", e)
    null
}

class SpiralConsoleCommandSettingsEditor(
    private val project: Project,
) : SettingsEditor<SpiralConsoleCommandRunConfiguration>() {
    private val commandNameField = JTextField()
    private val phpCommandLineConfigurationEditor = PhpCommandLineConfigurationEditor()

    private val myPanel: DialogPanel = panel {
        row {
            cell(commandNameField)
                .label(SpiralBundle.message("run.config.editor.command.label"), LabelPosition.LEFT)
                .align(Align.FILL)
        }.topGap(TopGap.MEDIUM)

        row {
            phpCommandLineConfigurationEditor.init(project, true)
            phpCommandLineConfigurationEditor.getMainPanel()?.let { panel ->
                scrollCell(panel).align(Align.FILL)
            }
        }.topGap(TopGap.MEDIUM)
    }

    override fun resetEditorFrom(spiralConsoleCommandRunConfiguration: SpiralConsoleCommandRunConfiguration) {
        val settings = spiralConsoleCommandRunConfiguration.settings

        myPanel.reset()
        commandNameField.text = settings.commandName.orEmpty()
        phpCommandLineConfigurationEditor.resetEditorFrom(settings.commandLineSettings)
    }

    override fun applyEditorTo(spiralConsoleCommandRunConfiguration: SpiralConsoleCommandRunConfiguration) {
        val settings = spiralConsoleCommandRunConfiguration.settings

        settings.commandName = commandNameField.text
        phpCommandLineConfigurationEditor.applyEditorTo(settings.commandLineSettings)
    }

    override fun createEditor() = myPanel
}
