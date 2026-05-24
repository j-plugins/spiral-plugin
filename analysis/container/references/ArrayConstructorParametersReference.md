# Analysis: src/main/kotlin/com/github/xepozz/spiral/container/references/ArrayConstructorParametersReference.kt

## Summary
Polyvariant reference for constructor parameter names used in DI container `Autowire` binding arrays. Resolves string keys in array constructors to matching constructor parameters of a target class.

## Issues

### 1. [STYLE] Debug `println` statements in production code
- Location: ArrayConstructorParametersReference.kt:18, 25
- Problem: `println("lookup for class: ...")` and `println("variants: ...")` remain in code.
- Why it matters: CLAUDE.md explicitly forbids `println` in new code.
- Suggested fix: Remove or replace with `Logger.getInstance(...).debug(...)`.

### 2. [PERF] PhpIndex lookup in `getVariants` is not cached
- Location: ArrayConstructorParametersReference.kt:19-28
- Problem: `PhpIndex.getInstance(project).getClassesByFQN(classFqn)` runs on every variants request without caching.
- Why it matters: Completion can call `getVariants()` repeatedly.
- Suggested fix: Cache via `CachedValuesManager.getCachedValue(element) { ... }`.

### 3. [STYLE] Dead commented code
- Location: ArrayConstructorParametersReference.kt:27, 29, 33
- Problem: Commented alternative implementations remain.
- Suggested fix: Remove.

### 4. [API-MISUSE] Element validity not re-checked after suspension
- Location: ArrayConstructorParametersReference.kt:19, 38
- Problem: No `element.isValid` guard before PSI traversal.
- Suggested fix: `if (!element.isValid) return emptyArray()` at start of both methods.

### 5. [MAINTAINABILITY] `calculateDefaultRangeInElement` vs `getRangeInElement` confusion
- Location: ArrayConstructorParametersReference.kt:33-35
- Problem: Commented `getRangeInElement()` next to active `calculateDefaultRangeInElement()` is confusing.
- Suggested fix: Remove the comment, keep only the override matching the base class contract.
