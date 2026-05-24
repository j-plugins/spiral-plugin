# Analysis: src/main/kotlin/com/github/xepozz/spiral/cqrs/CqrsIndexUtil.kt

## Summary
Utility object providing high-level queries to resolve CQRS command and query handlers via file-based indexes. Wraps `FileBasedIndex` calls to `CqrsCommandIndex`, `CqrsQueryIndex`, and their handler-side indexes.

## Issues

### 1. [DUMB-MODE] No dumb-mode guard on FileBasedIndex access
- Location: CqrsIndexUtil.kt:10-22
- Problem: Both `findCommandHandlers()` and `findQueryHandlers()` call `FileBasedIndex.getInstance().getValues()` unconditionally. The IntelliJ Platform explicitly forbids index access during dumb mode (file indexing in progress). This is a common error in plugins but violates the documented contract.
- Why it matters: Callers (e.g., `LineMarkerProvider`, `ReferenceContributor`) may invoke these methods during indexing, causing `IndexNotReadyException` and potential UI crashes or deadlocks.
- Suggested fix: Add dumb-mode checking to both methods:
  ```kotlin
  import com.intellij.openapi.project.DumbService
  
  fun findCommandHandlers(command: String, project: Project): Collection<String> {
      if (DumbService.isDumb(project)) return emptyList()
      val fileBasedIndex = FileBasedIndex.getInstance()
      return fileBasedIndex.getValues(CqrsCommandIndex.key, command, GlobalSearchScope.allScope(project))
  }
  
  fun findQueryHandlers(command: String, project: Project): Collection<String> {
      if (DumbService.isDumb(project)) return emptyList()
      val fileBasedIndex = FileBasedIndex.getInstance()
      return fileBasedIndex.getValues(CqrsQueryIndex.key, command, GlobalSearchScope.allScope(project))
  }
  ```

