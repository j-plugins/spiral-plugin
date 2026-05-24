# Analysis: src/main/kotlin/com/github/xepozz/spiral/scaffolder/project/SpiralGeneratorProjectWizard.kt

## Summary
This class is the entry point for the Spiral project generator in the IDE's "New Project" wizard. It extends `WebTemplateNewProjectWizardBase` and wires together the project wizard UI steps and the underlying template generator.

## Issues

### 1. [STYLE] Missing user-facing string for wizard name
- Location: line 13
- Problem: The `name` property returns a hard-coded string `"Spiral"` instead of using `SpiralBundle.message(...)`.
- Why it matters: Per CLAUDE.md, all user-visible strings must be localized via `SpiralBundle.message(key, ...)` for i18n support.
- Suggested fix: Add a key to `SpiralBundle.properties` (e.g., `spiral.project.wizard.name=Spiral`) and return `SpiralBundle.message("spiral.project.wizard.name")` from the property.

## No further significant issues found.
