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

    override fun getHelpCommandPlaceholder() = "spiral <command>"

    override fun getCompletionGroupTitle() = "Spiral"

    override fun getHelpCommand() = "spiral"

    override fun getHelpGroupTitle() = "PHP"

    override fun getHelpIcon() = SpiralIcons.SPIRAL

    override fun getIcon(value: SpiralRunCommandAction) = AllIcons.Actions.Execute

    override fun getValues(dataContext: DataContext, pattern: String): Collection<SpiralRunCommandAction> {
        val project = CommonDataKeys.PROJECT.getData(dataContext) ?: return emptyList()
        if (DumbService.isDumb(project)) return emptyList()

        return ReadAction.compute<Collection<SpiralRunCommandAction>, Throwable> {
            FileBasedIndex.getInstance()
                .getAllKeys(ConsoleCommandsIndex.key, project)
                .map { SpiralRunCommandAction(it) }
                .sortedBy { it.commandName }
        }
    }
}