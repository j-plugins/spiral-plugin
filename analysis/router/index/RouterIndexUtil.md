# Analysis: src/main/kotlin/com/github/xepozz/spiral/router/index/RouterIndexUtil.kt

## Summary
Utility singleton providing helper methods to parse route attributes (methods, content), retrieve all routes from the index, and look up controller methods by FQN.

## Issues

### 1. [API-MISUSE] Index queries without dumb mode check
- Location: RouterIndexUtil.kt:30-40
- Problem: `getAllRoutes` calls `FileBasedIndex.getInstance().getAllKeys(...)` and `getValues(...)` without checking `DumbService.isDumb(project)`. These calls are forbidden during indexing.
- Why it matters: Will throw `IndexNotReadyException` if called while IDE is indexing. This method is used by `SpiralEndpointsProvider`, `RouterGroupReference`, `RouterNameReference`, and `RouterUriReference` which may be invoked during indexing.
- Suggested fix: Guard with `if (DumbService.isDumb(project)) return emptyList()` at the start of `getAllRoutes`.

### 2. [PERF] Repeated index queries without caching
- Location: RouterIndexUtil.kt:30-40
- Problem: `getAllRoutes` iterates through all keys in the index and performs a `getValues` lookup for each, resulting in O(n) index queries. This is called repeatedly by reference contributors and the endpoints provider.
- Why it matters: Can be slow for large route catalogs. No caching layer exists to avoid repeated queries.
- Suggested fix: Cache results using `CachedValuesManager.getManager(project).createCachedValue { ... }` with invalidation on `PsiModificationTracker`.

### 3. [PERF] FQN splitting without error handling
- Location: RouterIndexUtil.kt:42-49
- Problem: `getControllerMethodByFqn` splits FQN by '.' but silently returns `emptyList()` if the size is not exactly 2. No logging provides feedback on malformed FQNs, and the split assumes dots are separators (PHP uses backslashes).
- Why it matters: If the FQN format is inconsistent (e.g., contains backslashes from `PhpClass.fqn`), the method will fail silently and navigation will not work.
- Suggested fix: Document the expected FQN format. Consider using `PhpIndexImpl` to look up the class directly if the FQN is in namespace format.

### 4. [MAINTAINABILITY] Hardcoded HTTP verbs list
- Location: RouterIndexUtil.kt:14
- Problem: `ALL_VERBS` is hardcoded and missing some HTTP methods like `TRACE` and `CONNECT`. If Spiral adds support for additional methods, this list must be updated.
- Why it matters: Completion suggestions will be incomplete if methods are added to the framework.
- Suggested fix: Add a comment noting that this list is based on Spiral's documented HTTP methods, and check the framework docs for updates.

### 5. [STYLE] Incomplete todo comment
- Location: RouterIndexUtil.kt:16
- Problem: `// todo: check for ClasConstantReference` suggests unfinished work, but no context is provided.
- Why it matters: Unclear what this todo refers to or its priority.
- Suggested fix: Either complete the todo and remove the comment, or expand it with context (e.g., "TODO: Handle method names as class constants, e.g., `UserController::DETAIL_ACTION`").
