# Analysis: src/main/kotlin/com/github/xepozz/spiral/views/references/ViewNamespaceReference.kt

## Summary
This PSI reference resolves view namespace names (the part before the colon in `namespace:filename`) to their corresponding filesystem directories. It provides completion variants from the indexed namespaces.

## Issues

### 1. [STYLE] Commented-out debug statements
- Location: lines 29, 42
- Problem: Two `println(...)` statements are commented out instead of being removed.
- Why it matters: Commented-out code reduces clarity and suggests incomplete work.
- Suggested fix: Remove these commented lines entirely.

### 2. [PERF] Index lookup in getVariants without dumb mode check
- Location: lines 43-44
- Problem: The `getVariants()` method calls `ViewsNamespaceIndexUtil.getAllNamespaces(project)`, which accesses `FileBasedIndex` without dumb mode protection (as noted in the `ViewsNamespaceIndexUtil` analysis).
- Why it matters: This will fail or block during indexing if called during dumb mode.
- Suggested fix: Ensure `ViewsNamespaceIndexUtil` implements dumb mode checks, or guard this call with a dumb mode check.

### 3. [STYLE] Hard-coded user-visible string
- Location: line 50
- Problem: The type text `"Namespace"` is a hard-coded string instead of being localized via `SpiralBundle.message(...)`.
- Why it matters: Per CLAUDE.md, all user-visible strings must use `SpiralBundle.message(...)` for i18n support.
- Suggested fix: Add a key to `SpiralBundle.properties` (e.g., `spiral.namespace.type=Namespace`) and use `SpiralBundle.message("spiral.namespace.type")`.

## No further significant issues found.
