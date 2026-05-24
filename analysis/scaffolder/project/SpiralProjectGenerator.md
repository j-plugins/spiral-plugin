# Analysis: src/main/kotlin/com/github/xepozz/spiral/scaffolder/project/SpiralProjectGenerator.kt

## Summary
This class manages Spiral project generation via Composer. It orchestrates detection of the Composer executable (falling back to Phar if needed) and delegates to `ComposerProjectGenerator` to install the framework template.

## Issues

### 1. [STYLE] Banned logging pattern: println used instead of Logger
- Location: lines 39, 53
- Problem: The code uses `println(...)` for debug output instead of the proper `com.intellij.openapi.diagnostic.Logger`.
- Why it matters: Per CLAUDE.md, `println` is forbidden in new code. Debug logs should use the IntelliJ Platform logger for filtering and management.
- Suggested fix: Create a companion-object logger (`Logger.getInstance(SpiralProjectGenerator::class.java)`) and replace both `println` calls with `logger.debug(...)`.

### 2. [MAINTAINABILITY] Hard-coded user-visible string
- Location: line 58
- Problem: The description `"Spiral Framework project template"` is a hard-coded string instead of being localized via `SpiralBundle.message(...)`.
- Why it matters: All user-facing strings must go through `SpiralBundle.message(...)` for i18n.
- Suggested fix: Add a key to `SpiralBundle.properties` (e.g., `spiral.project.generator.description=Spiral Framework project template`) and return `SpiralBundle.message("spiral.project.generator.description")`.

### 3. [MAINTAINABILITY] Hard-coded template name
- Location: line 17
- Problem: The template name `"Spiral"` is hard-coded in the `getName()` method instead of being localized.
- Why it matters: Consistency with i18n best practices; should align with the wizard name.
- Suggested fix: Return `SpiralBundle.message("spiral.project.generator.name")` instead.

## No further significant issues found.
