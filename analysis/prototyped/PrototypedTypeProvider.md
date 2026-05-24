# Analysis: src/main/kotlin/com/github/xepozz/spiral/prototyped/PrototypedTypeProvider.kt

## Summary
This type provider infers the type of field references (`$this->fieldName`) in classes with `PrototypeTrait`, returning the FQN of the prototype class registered in the `PrototypedIndex`. It enables type-aware IDE features like code completion and refactoring for dynamic properties.

## Issues

### 1. [STYLE] Commented-out debug println
- Location: line 48
- Problem: `println("prototype fieldName: $fieldName class: $phpClass")` is commented out.
- Why it matters: Per CLAUDE.md conventions, no `println` statements should remain in new code. Commented-out debug code clutters the codebase.
- Suggested fix: Remove the commented line entirely.

### 2. [PERF] Index lookups could be cached
- Location: lines 36-46
- Problem: For the same `fieldName` in the same project/file, `PrototypedIndex.getPrototypes(project)` and `PrototypedIndex.getPrototypeClass(fieldName, project)` are called multiple times (lines 36, 40) and `PhpIndex.getInstance(project)` (line 42) performs a linear search over all classes matching the FQN. In a large codebase, this can be slow if `getType()` is called frequently.
- Why it matters: Type providers are called during code completion, highlighting, and IDE inspections. Repeated index queries without caching can slow down the IDE.
- Suggested fix: Consider caching the result of the prototype lookup using `CachedValuesManager.getManager(project).getCachedValue(...)`. Note: Be mindful of cache invalidation; the cache should depend on the index generation.

### 3. [API-MISUSE] Incomplete implementations of abstract methods
- Location: lines 53-63
- Problem: `complete()` and `getBySignature()` methods return `null` unconditionally, with no implementation. These are required by `PhpTypeProvider4` interface but are not used by the type provider pattern shown here (which relies on `getType()`).
- Why it matters: These methods are part of the interface and should have at least minimal implementations. If they are intentionally stubbed, the IDE may not correctly handle type signatures in edge cases.
- Suggested fix: Either implement these methods properly if they are needed for the type provider to work correctly, or document why they are not used. For now, returning `null` is acceptable if `getType()` is the only method that matters for this provider's use case, but this should be verified against PhpStorm's documentation.

### 4. [MAINTAINABILITY] Magic character constant for getKey()
- Location: line 22
- Problem: `getKey()` returns `'Ꙅ'`, a Unicode character. This magic value is not documented, and it's unclear why this specific character was chosen.
- Why it matters: If multiple type providers use the same key character, they will conflict. The key is not self-documenting.
- Suggested fix: Either document why this specific character was chosen, or use a more descriptive constant (e.g., a string-based key if the API supports it). Ensure this character is unique across all type providers in the plugin.

### 5. [THREAD] Potential race condition in getType()
- Location: lines 24-50
- Problem: `getType()` performs multiple PSI reads (`getParentOfType`, `hasTrait`) and index queries (`PrototypedIndex.getPrototypes`, `PrototypedIndex.getPrototypeClass`, `PhpIndex.getInstance`) without an explicit read action. While the IDE may implicitly provide read access for type providers, it's not guaranteed.
- Why it matters: If the PSI tree is modified concurrently (e.g., during file editing), the method may return stale or incorrect types, or throw `PsiInvalidElementAccessException`.
- Suggested fix: Wrap the PSI traversal in a read action if not already implicit: `ReadAction.compute { ... }`. Verify with PhpStorm's documentation if type providers have implicit read access.
