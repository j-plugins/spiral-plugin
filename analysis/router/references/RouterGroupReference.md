# Analysis: src/main/kotlin/com/github/xepozz/spiral/router/references/RouterGroupReference.kt

## Summary
Provides code completion for route group names in `@Route` attributes, sourcing available groups from the router index. A soft reference with no resolve functionality.

## Issues

### 1. [PERF] Undeduplicating variant list
- Location: RouterGroupReference.kt:20-30
- Problem: `getVariants()` fetches all routes via `RouterIndexUtil.getAllRoutes(...)` and extracts unique group names. If there are 100 routes, the list may contain 30 unique groups, but all routes are iterated even after all groups are discovered.
- Why it matters: For large route catalogs, this is inefficient. No early-exit mechanism exists.
- Suggested fix: Collect groups into a `Set<String>` and convert to lookup elements, or use `toSet()` before mapping to `LookupElementBuilder`.

### 2. [PERF] Index query on EDT
- Location: RouterGroupReference.kt:21-22
- Problem: `getVariants()` is called during code completion, which occurs on the EDT. It queries the file-based index synchronously without checking dumb mode.
- Why it matters: Large indexes will freeze the UI during completion.
- Suggested fix: Guard with `DumbService.isDumb(element.project)` check. Consider using background execution or caching.

### 3. [MAINTAINABILITY] Null group handling
- Location: RouterGroupReference.kt:23-24
- Problem: Routes with `group == null` are skipped via `mapNotNull`, but no comment explains why null groups are excluded from completion.
- Why it matters: Users may want to explicitly reference the implicit "Root" group.
- Suggested fix: Add a comment explaining the null group behavior, or include null/empty string as a completion option with description "Root group".

### 4. [STYLE] Missing lookup string customization
- Location: RouterGroupReference.kt:26-27
- Problem: `LookupElementBuilder.create(route.group)` uses the group name as both the lookup string and the presentation text. No case-insensitive variant or tail text is provided.
- Why it matters: Completion is case-sensitive, which may be unexpected for group names.
- Suggested fix: Add `.withLookupString(route.group.lowercase())` to support case-insensitive completion.
