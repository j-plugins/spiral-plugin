package com.github.xepozz.spiral.scaffolder.project

import com.github.xepozz.spiral.SpiralBundle
import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.ide.util.projectWizard.WebProjectTemplate
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.php.composer.ComposerProjectGenerator
import com.jetbrains.php.composer.ComposerProjectSettings
import com.jetbrains.php.composer.addDependency.ComposerPackage
import com.jetbrains.php.composer.execution.ComposerExecution
import com.jetbrains.php.composer.execution.executable.ExecutableComposerExecution
import com.jetbrains.php.composer.execution.phar.PharComposerExecution
import com.jetbrains.php.config.interpreters.PhpInterpretersManagerImpl

class SpiralProjectGenerator : WebProjectTemplate<SpiralProjectGeneratorSettings>() {
    override fun getName(): String = SpiralBundle.message("spiral.project.generator.name")

    override fun generateProject(
        project: Project,
        baseDir: VirtualFile,
        settings: SpiralProjectGeneratorSettings,
        module: Module
    ) {
        val isDownload = PathEnvironmentVariableUtil.findExecutableInPathOnAnyOS("composer") == null
        val composerExecutable = when {
            isDownload -> {
                val phpInterpretersManager = PhpInterpretersManagerImpl.getInstance(project)
                PharComposerExecution(
                    phpInterpretersManager.interpreters.firstOrNull()?.id,
                    "",
                    false,
                )
            }

            else -> ExecutableComposerExecution("composer")
        }

        LOG.debug { "generateProject (pre): baseDir=$baseDir, settings=$settings, module=$module" }
        val version = if (settings.version != "latest") settings.version else null

        val composerSettings = ComposerProjectSettings(
            isDownload,
            ComposerPackage(settings.template),
            version,
            "--no-progress --no-interaction --ansi --remove-vcs --stability=dev",
            composerExecutable as ComposerExecution,
            null,
            settings.createGit,
        )
        ComposerProjectGenerator().generateProject(project, baseDir, composerSettings, module)

        LOG.debug { "generateProject (post): baseDir=$baseDir, settings=$settings, module=$module" }
    }

    override fun createPeer() = SpiralProjectPeer()

    override fun getDescription(): String = SpiralBundle.message("spiral.project.generator.description")

    companion object {
        private val LOG = Logger.getInstance(SpiralProjectGenerator::class.java)
    }
}