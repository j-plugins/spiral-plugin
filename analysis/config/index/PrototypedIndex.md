# Analysis: src/main/kotlin/com/github/xepozz/spiral/config/index/PrototypedIndex.kt

## Summary
Indexes `@Prototyped` attributes and the `DEFAULT_SHORTCUTS` from `PrototypeBootloader`, mapping prototype property names to their resolved class FQNs.

## Issues

### 1. [DUMB-MODE] Index queries lack dumb-mode guards
- Location: PrototypedIndex.kt:37, 43, 45, 52
- Problem: Companion-object methods call `FileBasedIndex.getInstance().getValues(...)` and `getAllKeys(...)` without `DumbService` checks.
- Why it matters: Throws `IndexNotReadyException` during indexing; callers from contributors/line markers must guard.
- Suggested fix: Wrap calls in `DumbService.getInstance(project).runReadActionInSmartMode { ... }`, or document that callers must do so.

### 2. [STYLE] Commented debug println
- Location: PrototypedIndex.kt:88
- Problem: `println("predefinedShortcuts: $result")` is commented but left in the file.
- Suggested fix: Delete.

### 3. [STYLE] Multiple commented-out code blocks
- Location: PrototypedIndex.kt:39, 106
- Problem: Dead alternative implementations.
- Suggested fix: Delete.

### 4. [INDEX] Version bumped to 2 without documented reason
- Location: PrototypedIndex.kt:57
- Problem: `getVersion()` returns 2 with no comment explaining the schema change.
- Why it matters: Future maintainers won't know when to bump again.
- Suggested fix: Add a one-line comment: "v2: switched value shape from X to Y".

### 5. [MAINTAINABILITY] Shadowed `it` in nested lambdas
- Location: PrototypedIndex.kt:29, 38, 48
- Problem: Multiple nested `it` bindings reduce readability.
- Suggested fix: Use named lambda parameters (`proto ->`, `entry ->`).
