# Analysis: src/main/kotlin/com/github/xepozz/spiral/router/endpoints/SpiralEndpointsProvider.kt

## Summary
Implements the microservices endpoints provider interface to expose HTTP routes discovered via the router index to the IDE's Endpoints tool window. Retrieves routes from `RouterIndexUtil`, groups them, and provides navigation to controller methods.

## Issues

### 1. [BUG] Debug println left in production code
- Location: SpiralEndpointsProvider.kt:37
- Problem: `println("groups $this ${this.map { it.group to it.routes.size }}")` is a debug statement that will print to stderr on every endpoints query.
- Why it matters: Per CLAUDE.md coding conventions, `println` is forbidden in new code; violates the no-logging rule. Clutters user output and degrades performance.
- Suggested fix: Remove the debug statement entirely, or replace with `Logger.getInstance(...).debug(...)` if debugging is needed.

### 2. [PERF] Index query on EDT in tool window provider
- Location: SpiralEndpointsProvider.kt:28-37
- Problem: `getEndpointGroups` calls `RouterIndexUtil.getAllRoutes(project)` which uses `FileBasedIndex.getInstance().getValues(...)`. This runs synchronously on the EDT when the tool window is populated/updated, potentially blocking the UI.
- Why it matters: Large route catalogs will freeze the UI during navigation or tool window refresh.
- Suggested fix: Move index query to a background thread via `ReadAction.nonBlocking()`, and cache results with proper invalidation based on `PsiModificationTracker`.

### 3. [API-MISUSE] Dumb mode not handled
- Location: SpiralEndpointsProvider.kt:28-37
- Problem: `getEndpointGroups` queries `FileBasedIndex` without checking `DumbService.isDumb(project)`. During indexing, `FileBasedIndex.getValues()` is forbidden.
- Why it matters: Will crash with `IndexNotReadyException` if the endpoints provider is invoked while the IDE is indexing files.
- Suggested fix: Guard the index query with `DumbService.isDumb(project)` check, returning `emptyList()` if true.

### 4. [MAINTAINABILITY] Hardcoded FQN parsing logic
- Location: SpiralEndpointsProvider.kt:52
- Problem: `endpoint.fqn.replace(".", "::")` assumes FQN is in PHP namespace format (with dots) and converts it to IDE presentation format (with `::`). This logic should be centralized.
- Why it matters: If the FQN format changes or is used elsewhere, the logic will be duplicated and hard to maintain.
- Suggested fix: Move the conversion to `RouterIndexUtil` or create a utility function in `php/mixin.kt`.

### 5. [STYLE] Incomplete commented-out code
- Location: SpiralEndpointsProvider.kt:42
- Problem: `isValidEndpoint` returns a hardcoded `true` with a commented-out `group.isValid()` check. It's unclear why this is disabled.
- Why it matters: Suggests unfinished implementation or a workaround; makes future maintainers uncertain about the intended behavior.
- Suggested fix: Either restore the `group.isValid()` check if it's needed, or clarify why all endpoints are always valid with a comment.

### 6. [MAINTAINABILITY] Missing null checks in navigation
- Location: SpiralEndpointsProvider.kt:44-46, 57-59
- Problem: `getDocumentationElement` and `getNavigationElement` both call `RouterIndexUtil.getControllerMethodByFqn(...).firstOrNull()`, which will return null if the FQN is malformed or the method doesn't exist. The tool window will silently fail to navigate.
- Why it matters: Poor user experience if navigation fails without feedback.
- Suggested fix: No explicit fix required at this level, but ensure FQN format is validated earlier in the pipeline (e.g., in `AbstractRouterIndex`).
