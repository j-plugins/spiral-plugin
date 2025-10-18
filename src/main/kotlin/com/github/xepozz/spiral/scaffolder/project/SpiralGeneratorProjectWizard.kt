package com.github.xepozz.spiral.scaffolder.project

import com.github.xepozz.spiral.SpiralIcons
import com.github.xepozz.spiral.scaffolder.project.override.WebTemplateProjectWizardStep
import com.intellij.ide.util.projectWizard.WebTemplateNewProjectWizardBase
import com.intellij.ide.wizard.NewProjectWizardBaseStep
import com.intellij.ide.wizard.NewProjectWizardChainStep.Companion.nextStep
import com.intellij.ide.wizard.NewProjectWizardStep
import com.intellij.ide.wizard.RootNewProjectWizardStep

class SpiralGeneratorProjectWizard : WebTemplateNewProjectWizardBase() {
    override val id = "spiral-project"
    override val name = "Spiral"
    override val icon = SpiralIcons.SPIRAL

    val template = SpiralProjectGenerator()

    override fun createTemplateStep(parent: NewProjectWizardBaseStep): NewProjectWizardStep {
        return RootNewProjectWizardStep(parent.context)
            .nextStep { WebTemplateProjectWizardStep(parent, template) }
    }
}