# Analysis: src/main/kotlin/com/github/xepozz/spiral/prototyped/PrototypedPropertyReference.kt

## Summary
This reference class represents a property name reference within a prototype context. It provides resolution and completion variants for prototype properties indexed by `PrototypedIndex`, but currently has a stub implementation.

## Issues

### 1. [BUG] Incomplete resolve() implementation
- Location: lines 14-23
- Problem: The `resolve()` method returns null with all resolution logic commented out. The class is instantiated but never resolves to any actual PHP element.
- Why it matters: References that always return null prevent IDE features like "Go to Definition", "Find Usages", and rename refactoring from working correctly.
- Suggested fix: Uncomment and complete the resolve logic. The commented code suggests: (1) get the prototype FQN from the index for the given property, (2) look up the class via `PhpIndex.getAnyByFQN()`, and (3) return the resolved class element. Consider whether resolving to the class definition is the intended target or if a property within that class should be returned instead.

### 2. [BUG] isSoft() always returns true without resolution
- Location: line 25
- Problem: Setting `isSoft() = true` tells the IDE "this reference may not resolve, which is OK." Combined with an always-null `resolve()`, this disables all IDE navigation and refactoring for this reference.
- Why it matters: Users cannot click on prototype property names to jump to their definitions, and IDE refactoring (rename, find usages) will not work.
- Suggested fix: Set `isSoft() = false` once `resolve()` is properly implemented and can resolve most references. If some references are expected to be unresolvable, use a more nuanced approach (e.g., check if the property exists in the index before deciding).

### 3. [API-MISUSE] getVariants() does not use PhpIndex
- Location: lines 28-32
- Problem: `PrototypedIndex.getPrototypes(element.project)` is called and assigned to `properties`, then `PhpIndex` is retrieved but never used. The method returns `properties.toTypedArray()` without enriching the variants with type information or metadata. The `phpIndex` variable is unused.
- Why it matters: Wasted code; completion items may lack context or type information that would be helpful to users. The unused `phpIndex` suggests incomplete implementation.
- Suggested fix: Either remove the unused `phpIndex` and ensure `getVariants()` returns adequate completion items (consider wrapping them in `LookupElementBuilder` with type text, as done in `PrototypedCompletion`), or complete the implementation to use `phpIndex` to enrich the variants with type information.

### 4. [MAINTAINABILITY] Dead code in companion object
- Location: line 35
- Problem: The companion object is empty (only has `}`), suggesting either incomplete refactoring or code removal.
- Why it matters: Empty companion objects add visual clutter and suggest unfinished work.
- Suggested fix: Remove the empty companion object block.

### 5. [UNUSED] Unused class parameter 'property'
- Location: line 10
- Problem: The property `val property: String` is stored but never used in the class methods.
- Why it matters: Suggests incomplete refactoring or unfinished implementation. The property may have been intended for use in `resolve()` or other methods.
- Suggested fix: Use `property` in the `resolve()` method to look up the prototype class, or remove it if no longer needed. The parameter should be leveraged once the stub implementation is completed.

### 6. [DUMB-MODE] No dumb-mode guard on index access
- Location: lines 29-31
- Problem: `PrototypedIndex.getPrototypes(element.project)` calls `FileBasedIndex.getInstance().getAllKeys(...)` without checking `DumbService.isDumb(project)`. Index queries are forbidden in dumb mode.
- Why it matters: During indexing, this can fail or return incomplete data.
- Suggested fix: Guard with `DumbService.isDumb(project)` or wrap in `DumbService.runReadActionInSmartMode { ... }`.
