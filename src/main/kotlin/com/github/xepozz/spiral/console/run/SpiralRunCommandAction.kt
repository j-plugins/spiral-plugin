package com.github.xepozz.spiral.console.run

import com.github.xepozz.spiral.SpiralBundle
import com.intellij.execution.Executor
import com.intellij.execution.RunManagerEx
import com.intellij.execution.runners.ExecutionUtil
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger

private val LOG = Logger.getInstance(SpiralRunCommandAction::class.java)

class SpiralRunCommandAction(val commandName: String) : AnAction() {
    init {
        templatePresentation.setText(SpiralBundle.message("action.run.target.text", commandName), false)
        templatePresentation.description = SpiralBundle.message("action.run.target.description", commandName)
        templatePresentation.icon = AllIcons.Actions.Execute
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return

        val executor = Executor.EXECUTOR_EXTENSION_NAME.extensionList.firstOrNull()
        if (executor == null) {
            LOG.warn("No executors registered; cannot run Spiral command '$commandName'")
            return
        }

        val runManager = RunManagerEx.getInstanceEx(project)
        val configurationFactory = SpiralConsoleCommandRunConfigurationType.INSTANCE.configurationFactories.first()

        val runConfiguration = SpiralConsoleCommandRunConfiguration(
            project,
            configurationFactory,
            SpiralBundle.message("action.run.target.command", commandName),
        ).apply { settings.commandName = commandName }

        val configuration = runManager.createConfiguration(runConfiguration, configurationFactory)

        runManager.setTemporaryConfiguration(configuration)
        ExecutionUtil.runConfiguration(configuration, executor)
    }
}
