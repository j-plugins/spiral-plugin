# Analysis: console/run/SpiralConsoleCommandRunConfigurationSettings.kt

## Summary
This class stores persistent settings for Spiral console command run configurations, extending `PhpRunConfigurationSettings` and `LocatableRunConfigurationOptions`. It manages command name, binary path, working directory, and delegated PHP command-line settings.

## Issues

### 1. [BUG] Incorrect property delegation for myWorkingDirectory

- Location: line 10
- Problem: The property `myWorkingDirectory` is delegated using `string("").provideDelegate(this, "binary")` — but the name argument is `"binary"`, not `"workingDirectory"`. This causes the working directory to be persisted under the wrong XML attribute name and share state with the binary property.
- Why it matters: The working directory setting will not persist correctly across runs. It may also collide with or overwrite the `binary` property value.
- Suggested fix: Change to `string("").provideDelegate(this, "workingDirectory")`.

### 2. [LOGIC] documentRoot getter returns wrong property

- Location: lines 24-28
- Problem: The `documentRoot` property getter and setter both use `myBinary.getValue(this)` and `myBinary.setValue(this, ...)`, but they should use `myWorkingDirectory` (or a separate `myDocumentRoot` property if that's the intended semantics).
- Why it matters: Setting or reading `documentRoot` will incorrectly affect the `binary` property instead, causing configuration corruption.
- Suggested fix: Either define a separate `myDocumentRoot` property, or clarify the intent and use the correct property reference.

### 3. [MAINTAINABILITY] Unused import and unclear property semantics

- Location: lines 1-6 and throughout
- Problem: The file imports `PhpRunConfigurationSettings` but doesn't clearly indicate which inherited methods are overridden vs. which new properties are added. The class has both `binary` and `documentRoot` properties that appear to manage the same underlying value.
- Why it matters: Unclear property semantics make the configuration error-prone. Callers may not know which property to use for what purpose.
- Suggested fix: Add documentation comments to each property clarifying its purpose, or refactor to avoid shadowing properties.

### 4. [STYLE] var instead of val for non-mutating property

- Location: line 30
- Problem: `var commandLineSettings = PhpCommandLineSettings()` is declared as mutable, but there's no evidence it's reassigned after initialization. It could be `val`.
- Why it matters: Reduces clarity about what state is mutable, and increases risk of accidental reassignment.
- Suggested fix: Change to `val commandLineSettings = PhpCommandLineSettings()`.

