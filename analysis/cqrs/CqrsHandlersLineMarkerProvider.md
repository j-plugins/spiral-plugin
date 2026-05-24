# Analysis: src/main/kotlin/com/github/xepozz/spiral/cqrs/CqrsHandlersLineMarkerProvider.kt

## Summary
Provides IDE gutter line markers on CQRS command and query classes, displaying navigation icons that link to their corresponding handlers. Uses `CqrsIndexUtil` to resolve handlers and defers lookup via `NotNullLazyValue`.

## Issues

### 1. [DUMB-MODE] Missing dumb-mode guard on index access
- Location: CqrsHandlersLineMarkerProvider.kt:27-31
- Problem: `CqrsIndexUtil.findQueryHandlers()` and `CqrsIndexUtil.findCommandHandlers()` call `FileBasedIndex.getValues()` without checking if the project is in dumb mode. Line markers are called during indexing and may execute in dumb mode, where index queries are forbidden.
- Why it matters: Querying the index during dumb mode throws an exception, causing line marker rendering to fail and potentially crashing the IDE's UI thread.
- Suggested fix: Add a dumb-mode check before querying indexes:
  ```kotlin
  if (DumbService.isDumb(project)) return null
  
  val classes = if (isQuery) {
      CqrsIndexUtil.findQueryHandlers(element.fqn, project)
  } else {
      CqrsIndexUtil.findCommandHandlers(element.fqn, project)
  }
  ```

### 2. [I18N] Hard-coded tooltip string
- Location: CqrsHandlersLineMarkerProvider.kt:41
- Problem: The tooltip text `"Navigate to handler"` is hard-coded and not localized via `SpiralBundle.message()`.
- Why it matters: Users with non-English IDE locales will see English tooltip text, violating i18n principles and user experience consistency.
- Suggested fix: Extract the string to `SpiralBundle.properties` and reference it:
  ```kotlin
  .setTooltipText(SpiralBundle.message("action.navigate.handler"))
  ```

### 3. [STYLE] Unused icon comment
- Location: CqrsHandlersLineMarkerProvider.kt:38
- Problem: Comment `"// todo: replace with more suitable icon"` indicates incomplete implementation. The TODO should either be tracked as an issue or the icon should be finalized.
- Why it matters: TODOs in code suggest incomplete features and may confuse future maintainers about whether this is intentional or a regression.
- Suggested fix: Either replace `SpiralIcons.SPIRAL` with a more specific icon (e.g., `AllIcons.Gutter.ImplementingMethod`) or remove the comment if the icon choice is deliberate.

### 4. [MAINTAINABILITY] Magic string extraction logic
- Location: CqrsHandlersLineMarkerProvider.kt:49
- Problem: The `toClassFqn()` method extracts class FQN by removing the method part via `substringBeforeLast('.')`. This assumes all handlers have a method FQN (ending with `.methodName`), but the logic is not documented and is brittle.
- Why it matters: If a handler method name contains a dot, or if the FQN format changes, this extraction silently produces incorrect results without validation.
- Suggested fix: Add a more defensive extraction with validation or document why this is always safe:
  ```kotlin
  /**
   * Extracts class FQN from handler method FQN (e.g., `\Class\Name.__invoke` -> `\Class\Name`).
   * Assumes handlers are always methods (99% are `__invoke`).
   */
  private fun toClassFqn(fqn: String): String {
      val lastDot = fqn.lastIndexOf('.')
      return if (lastDot > 0) fqn.substring(0, lastDot) else fqn
  }
  ```

