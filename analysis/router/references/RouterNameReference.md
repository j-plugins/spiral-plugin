# Analysis: src/main/kotlin/com/github/xepozz/spiral/router/references/RouterNameReference.kt

## Summary
Provides code completion for route names in `@Route` attributes, sourcing available names from the router index. Displays route URIs as type text and groups as tail text for context.

## Issues

### 1. [PERF] Undeduplicating variant list
- Location: RouterNameReference.kt:20-32
- Problem: `getVariants()` fetches all routes and filters for non-null names, creating a lookup element for each route. If multiple routes share the same name, duplicate entries will appear in the completion list.
- Why it matters: Clutters the completion UI and slows rendering for large catalogs.
- Suggested fix: Use `distinctBy { it.name }` after filtering null names to eliminate duplicates.

### 2. [PERF] Index query on EDT
- Location: RouterNameReference.kt:21-22
- Problem: `getVariants()` queries `RouterIndexUtil.getAllRoutes(...)` synchronously during code completion, which runs on the EDT. No dumb-mode check exists.
- Why it matters: Will freeze the UI for projects with large route catalogs, especially during indexing.
- Suggested fix: Guard with `DumbService.isDumb(element.project)` check. Cache results using `CachedValuesManager`.

### 3. [MAINTAINABILITY] Nullable group in tail text
- Location: RouterNameReference.kt:28
- Problem: `withTailText(" [${route.group}]")` includes `route.group` which can be null, resulting in tail text like " [null]" for routes in the implicit root group.
- Why it matters: Poor UX; null groups should be displayed as "Root" or be omitted.
- Suggested fix: Use `route.group ?: "Root"` to provide a meaningful label for routes without an explicit group.

### 4. [STYLE] Missing lookup string customization
- Location: RouterNameReference.kt:26
- Problem: No case-insensitive lookup string is provided, making completion case-sensitive. Users must type the exact case.
- Why it matters: Routes may have names like "user.detail" and "User.Detail", requiring exact case matching.
- Suggested fix: Add `.withLookupString(route.name.lowercase())` to support case-insensitive completion.
