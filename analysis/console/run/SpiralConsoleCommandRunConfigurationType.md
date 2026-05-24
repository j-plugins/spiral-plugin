# Analysis: console/run/SpiralConsoleCommandRunConfigurationType.kt

## Summary
This class defines the run configuration type for Spiral console commands, registering a nested `ConfigurationFactory` and exposing the type via a static `INSTANCE` singleton. It pairs with the run configuration and settings editor to complete the run infrastructure.

## Issues

### 1. [STYLE] Hardcoded user-visible strings

- Location: lines 10-11
- Problem: The run type name and description are hardcoded: `"Spiral Command"` and `"Runs console command"`. These should be externalized to the bundle.
- Why it matters: Per project conventions (CLAUDE.md and context checklist), all user-facing strings must go through `SpiralBundle.message(...)` for i18n and maintainability.
- Suggested fix: Add keys to `SpiralBundle.properties` (e.g., `spiral.console.run.type.name`, `spiral.console.run.type.description`) and reference them: `SpiralBundle.message("spiral.console.run.type.name")`, etc.

### 2. [MAINTAINABILITY] Redundant factory ID constant

- Location: lines 15-16 and 26
- Problem: The factory's `getId()` method returns `ID`, which is defined separately in the companion object. The factory implementation is duplicated in both the init block (anonymous class) and the companion object constant.
- Why it matters: If `ID` changes, the factory won't automatically pick up the new value unless explicitly updated.
- Suggested fix: Refactor to extend a real `SpiralRunConfigurationFactory` class (which already exists in the codebase) rather than using an anonymous inner class. Or ensure the factory always reads `ID` from the companion object.

### 3. [API-MISUSE] ConfigurationFactory.getId() stability concern

- Location: line 16
- Problem: The `getId()` method is documented in the context as "must be stable across versions — changing it breaks user-saved configs". This implementation returns the constant `ID`, which is good, but there's no safeguard preventing accidental mutation of the constant.
- Why it matters: If someone refactors and accidentally changes the ID value, all existing user-saved configurations will break.
- Suggested fix: Mark the constant as final and immutable, or add a comment emphasizing the stability requirement.

