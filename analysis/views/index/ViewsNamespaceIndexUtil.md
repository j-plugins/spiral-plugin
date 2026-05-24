# Analysis: src/main/kotlin/com/github/xepozz/spiral/views/index/ViewsNamespaceIndexUtil.kt

## Summary
This utility object provides accessors for the `ViewsNamespaceIndex`, offering methods to query view namespace mappings by name or retrieve all mappings in a project.

## Issues

### 1. [DUMB-MODE] FileBasedIndex access without dumb mode protection
- Location: lines 28-32, 36-40
- Problem: The methods call `FileBasedIndex.getInstance().getValues(...)` and `getAllKeys(...)` without guarding against dumb mode. These operations are forbidden in dumb mode per the IntelliJ Platform checklist.
- Why it matters: Calling these methods during indexing (dumb mode) can block the indexing process and degrade IDE responsiveness.
- Suggested fix: Guard both methods with `DumbService.isDumb(project)` checks, or use `runReadActionInSmartMode(...)` to defer execution. Callers should expect `null` or empty results during dumb mode.

## No further significant issues found.
