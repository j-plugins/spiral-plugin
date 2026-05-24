# Analysis: src/main/kotlin/com/github/xepozz/spiral/SpiralIcons.kt

## Summary
Provides centralized icon access via `IconLoader.getIcon()`, serving as the single source of truth for the plugin's Spiral framework icon. Used throughout the plugin for completion items, gutter markers, and UI presentations.

## Issues

### 1. [API-MISUSE] IconLoader.getIcon() can return null; no null safety
- Location: SpiralIcons.kt:7
- Problem: `IconLoader.getIcon()` may return `null` if the resource is not found or unavailable, but the result is assigned directly to `val SPIRAL` with no null check. The field type is inferred as `Icon?` (nullable), which means all usages must handle potential null values.
- Why it matters: Any code that accesses `SpiralIcons.SPIRAL` without null-safety could crash at runtime if the icon fails to load. Completion items, gutter markers, and other UI elements expecting a non-null Icon would throw NPE.
- Suggested fix: Either (1) assert the result is non-null with `!!` and a comment if the icon is guaranteed to exist, (2) provide a fallback icon using `?:`, or (3) wrap access in a utility function that provides a safe default. Option 2 is preferred: `val SPIRAL = IconLoader.getIcon("/icons/spiral/icon.svg", javaClass) ?: AllIcons.General.Information` or similar.

