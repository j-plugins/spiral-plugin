# Analysis: src/main/kotlin/com/github/xepozz/spiral/SpellcheckingDictionaryProvider.kt

## Summary
Implements the IntelliJ Platform's BundledDictionaryProvider to register a spellcheck dictionary for the plugin. This is a minimal extension that points to the bundled dictionary resource.

## Issues

### 1. [MAINTAINABILITY] Path to dictionary file should be a constant
- Location: SpellcheckingDictionaryProvider.kt:6
- Problem: The dictionary path `/META-INF/spellcheck.dic` is hardcoded as a string literal instead of being defined as a constant, making it fragile if the resource is ever relocated.
- Why it matters: If the dictionary file moves, there's no single source of truth to update; the hardcoded string is easy to miss in refactoring.
- Suggested fix: Extract the path as a companion object constant or object-level constant, similar to how `SpiralBundle.BUNDLE` is defined in `SpiralBundle.kt`.

