package com.github.xepozz.spiral.console.run

import com.github.xepozz.spiral.SpiralBundle
import com.github.xepozz.spiral.SpiralIcons
import com.github.xepozz.spiral.console.index.ConsoleCommandsIndex
import com.intellij.icons.AllIcons
import com.intellij.ide.actions.runAnything.activity.RunAnythingAnActionProvider
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.DumbService
import com.intellij.util.indexing.FileBasedIndex

class SpiralRunAnythingProvider : RunAnythingAnActionProvider<SpiralRunCommandAction>() {
    override fun getCommand(value: SpiralRunCommandAction) =
        SpiralBundle.message("action.run.target.command", value.commandName)

    override fun getHelpCommandPlaceholder() = SpiralBundle.message("run.anything.placeholder")

    override fun getCompletionGroupTitle() = SpiralBundle.message("run.anything.group.title")

    override fun getHelpCommand() = SpiralBundle.message("run.anything.help.command")

    override fun getHelpGroupTitle() = SpiralBundle.message("run.anything.help.group.title")

    override fun getHelpIcon() = SpiralIcons.SPIRAL

    override fun getIcon(value: SpiralRunCommandAction) = AllIcons.Actions.Execute

    override fun getValues(dataContext: DataContext, pattern: String): Collection<SpiralRunCommandAction> {
        val project = CommonDataKeys.PROJECT.getData(dataContext) ?: return emptyList()
        if (DumbService.isDumb(project)) return emptyList()

        return ReadAction.compute<Collection<SpiralRunCommandAction>, Throwable> {
            // Re-check inside the read action — indexing could have started between the
            // outer check and acquiring the read lock.
            if (DumbService.isDumb(project)) return@compute emptyList()

            FileBasedIndex.getInstance()
                .getAllKeys(ConsoleCommandsIndex.key, project)
                .map { SpiralRunCommandAction(it) }
                .sortedBy { it.commandName }
        }
    }
}
