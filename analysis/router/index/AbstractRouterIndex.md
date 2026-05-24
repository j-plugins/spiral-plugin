# Analysis: src/main/kotlin/com/github/xepozz/spiral/router/index/AbstractRouterIndex.kt

## Summary
Abstract base class for router indexes that parses `@Route` attributes from PHP files and extracts route metadata (URI, name, methods, group). Extends `AbstractIndex` and is subclassed by `RouterNamesIndex` and `RouterUrlsIndex`.

## Issues

### 1. [INDEX] Version bump may be insufficient
- Location: AbstractRouterIndex.kt:24
- Problem: `getVersion()` returns `3`, but the change history is not documented. If the indexer output shape was modified between versions 1, 2, and 3, old caches will silently produce corrupt results unless version 4 is incremented when the shape changes again.
- Why it matters: Per CLAUDE.md and IntelliJ Platform best practices, version bumps must be tracked carefully. Without documentation, future changes risk data corruption.
- Suggested fix: Add a comment above `getVersion()` documenting why version is 3, e.g., "Version 3: Added support for group parameter parsing."

### 2. [PERF] No validation of parsed routes
- Location: AbstractRouterIndex.kt:32-52
- Problem: `parseRoutes` silently skips routes with empty `uri` (line 40), but doesn't validate method names or group names. Malformed routes are indexed without warning.
- Why it matters: Invalid routes (e.g., with empty URIs or invalid HTTP methods) will appear in completion suggestions and navigation, confusing users.
- Suggested fix: Add validation in `parseRoutes` to skip routes with empty URIs, or log a warning for diagnostic purposes.

### 3. [MAINTAINABILITY] Magic argument indices
- Location: AbstractRouterIndex.kt:40-43
- Problem: Routes are parsed using hardcoded argument indices (0, 1, 2, 4) which correspond to `uri`, `name`, `methods`, and `group`. The indices are not self-documenting and match the `@Route` attribute signature by convention only.
- Why it matters: If the `@Route` attribute signature changes in the Spiral framework, these indices become invalid and the indexer will silently produce empty values.
- Suggested fix: Add a comment above `parseRoutes` documenting the expected attribute signature and indices.

### 4. [MAINTAINABILITY] Unreliable type cast
- Location: AbstractRouterIndex.kt:38
- Problem: `attribute.owner as? Method ?: return@mapNotNull null` assumes the attribute owner is always a `Method`. While this is correct for `@Route`, if the attribute is misplaced (e.g., on a class), it will silently be skipped. There's no validation that the attribute is on a method.
- Why it matters: May mask configuration errors in user code without feedback.
- Suggested fix: Add a log message (using `Logger`) when an attribute is skipped due to incorrect placement.
