# Analysis: src/main/kotlin/com/github/xepozz/spiral/router/references/RouterUriReference.kt

## Summary
Provides code completion for route URIs in `@Route` attributes, sourcing available URIs from the router index. Displays route names as type text and groups as tail text for context.

## Issues

### 1. [PERF] Index query on EDT
- Location: RouterUriReference.kt:20-30
- Problem: `getVariants()` queries `RouterIndexUtil.getAllRoutes(...)` synchronously during code completion on the EDT. No dumb-mode check exists.
- Why it matters: Will freeze the UI for projects with large route catalogs, especially during indexing.
- Suggested fix: Guard with `DumbService.isDumb(element.project)` check. Cache results using `CachedValuesManager`.

### 2. [MAINTAINABILITY] Nullable group in tail text
- Location: RouterUriReference.kt:26
- Problem: `withTailText(" [${route.group}]")` includes `route.group` which can be null, resulting in tail text like " [null]" for routes in the implicit root group.
- Why it matters: Poor UX; null groups should be displayed as "Root" or be omitted.
- Suggested fix: Use `route.group ?: "Root"` to provide a meaningful label for routes without an explicit group.

### 3. [MAINTAINABILITY] Nullable name in type text
- Location: RouterUriReference.kt:25
- Problem: `withTypeText(route.name)` includes `route.name` which can be null. If no name is set, the type text will show "null".
- Why it matters: Poor UX; routes without names should either omit the type text or display a placeholder.
- Suggested fix: Use `.withTypeText(route.name ?: "")` to omit the type text for unnamed routes, or use a placeholder like `route.name ?: "<unnamed>"`.

### 4. [STYLE] Missing lookup string customization
- Location: RouterUriReference.kt:24
- Problem: No case-insensitive lookup string is provided, making completion case-sensitive. Users must type URIs with exact case.
- Why it matters: URIs are typically lowercase, but users may type "Users" expecting to match "/users".
- Suggested fix: Add `.withLookupString(route.uri.lowercase())` to support case-insensitive completion.

### 5. [PERF] No deduplication for duplicate URIs
- Location: RouterUriReference.kt:20-30
- Problem: If multiple routes share the same URI (e.g., GET /users and POST /users), both will appear as separate completion items with identical text.
- Why it matters: Clutters the completion list and slows rendering.
- Suggested fix: Either deduplicate by URI before mapping to lookup elements, or group by URI and show a combined entry with all methods.
