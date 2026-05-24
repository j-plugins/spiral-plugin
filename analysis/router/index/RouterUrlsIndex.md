# Analysis: src/main/kotlin/com/github/xepozz/spiral/router/index/RouterUrlsIndex.kt

## Summary
Indexes routes by their URI, allowing fast lookup of routes by URL path. Extends `AbstractRouterIndex` and provides the primary index for route discovery.

## Issues

### 1. [STYLE] Commented-out debug statement
- Location: RouterUrlsIndex.kt:28
- Problem: `// .apply { println("file: ${inputData.file}, result: $this") }` is commented-out debug code that should be removed.
- Why it matters: Clutters the code and suggests incomplete cleanup.
- Suggested fix: Remove the line entirely.

### 2. [INDEX] URI uniqueness not enforced
- Location: RouterUrlsIndex.kt:25-29
- Problem: Multiple routes with the same URI are indexed. `.associateBy { it.uri }` will silently keep only the last route for each URI when there are duplicates. This is likely unintended if multiple HTTP methods should map to the same URI.
- Why it matters: Routes with the same URI but different methods will be lost during indexing. For example, `GET /users` and `POST /users` will both map to the same key, and only one will be retained.
- Suggested fix: The index should probably map each URI to a list of routes, not a single route. Change return type to `Map<String, List<Route>>` and use `groupBy { it.uri }` instead of `associateBy`.
