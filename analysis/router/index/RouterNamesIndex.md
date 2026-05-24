# Analysis: src/main/kotlin/com/github/xepozz/spiral/router/index/RouterNamesIndex.kt

## Summary
Indexes routes by their optional `name` parameter, allowing lookup of routes by name. Extends `AbstractRouterIndex` and uses string keys for efficient name-based retrieval.

## Issues

### 1. [INDEX] Null keys in index
- Location: RouterNamesIndex.kt:28-31
- Problem: `parseRoutes` may include routes with `name = null` (line 30 in `AbstractRouterIndex`). When these are indexed with `.associateBy { it.name }`, the null keys are silently dropped by `associateBy` or included as null keys in the map.
- Why it matters: Routes without explicit names are excluded from the index, potentially causing inconsistencies if some queries expect all routes to be present.
- Suggested fix: Filter out routes with null names before associating: `.filter { it.name != null }.associateBy { it.name!! }`.

### 2. [STYLE] Commented-out debug statement
- Location: RouterNamesIndex.kt:31
- Problem: `// .apply { println("file: ${inputData.file}, result: $this") }` is commented-out debug code that should be removed.
- Why it matters: Clutters the code and suggests incomplete cleanup.
- Suggested fix: Remove the line entirely.
