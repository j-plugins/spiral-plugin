# Analysis: console/run/ConsoleCommandLineMarkerProvider.kt

## Summary
This line marker provider adds a run/debug gutter icon to Spiral console command classes (those with `#[AsCommand]` attribute). It reuses the existing executor action infrastructure to provide quick execution.

## Issues

### 1. [THREAD] No dumb-mode handling

- Location: lines 14-28
- Problem: The provider's `getInfo()` method calls `element.getConsoleCommandName()`, which traverses the PSI tree looking for `#[AsCommand]` attributes. This can be expensive and happens during indexing (dumb mode), but there's no check for `DumbService.isDumb()`.
- Why it matters: Per the context checklist, `LineMarkerProvider` may be invoked during indexing and should handle dumb mode gracefully. Expensive PSI traversal during indexing can block IDE responsiveness.
- Suggested fix: Add a dumb-mode guard at the start of `getInfo()`: `if (DumbService.isDumb(project)) return null`, or defer resolution via `NavigationGutterIconBuilder`.

### 2. [API-MISUSE] Type check is too broad

- Location: line 15
- Problem: The check `element !is PhpClass` returns null for any non-PhpClass element, but `getInfo()` is called on leaf elements (likely identifiers or method names), not the class itself. The expectation that the element IS a PhpClass is incorrect.
- Why it matters: Per the context checklist: "LineMarkerProvider.getLineMarkerInfo is invoked on the leaf element (not the whole class/function) — anchor to identifier, not container." This check will almost always return null unless the IDE happens to pass the class itself.
- Suggested fix: Navigate from the leaf element to its containing PhpClass: `val phpClass = PsiTreeUtil.getParentOfType(element, PhpClass::class.java) ?: return null`. Then apply the command name check on that class.

### 3. [I18N] Inconsistent key usage

- Location: lines 22-25
- Problem: The icon tooltip uses nested `SpiralBundle.message()` calls: `message("action.run.target.text", message("action.run.target.command", commandName))`. This creates compound keys that may not match the bundle file structure.
- Why it matters: The bundle only defines three keys: `action.run.target.text`, `action.run.target.command`, and `action.run.target.description`. Nesting one message inside another's placeholder is fragile and may result in unresolved keys or unexpected formatting.
- Suggested fix: Simplify to: `SpiralBundle.message("action.run.target.text", SpiralBundle.message("action.run.target.command", commandName))` — though this is what's already written. Actually, look at the bundle: `action.run.target.text=Run {0}`. So the fix is `SpiralBundle.message("action.run.target.text", commandName)` to avoid double-wrapping.

