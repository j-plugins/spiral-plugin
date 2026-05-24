# Analysis: src/main/kotlin/com/github/xepozz/spiral/cqrs/index/CqrsQueryHandlerIndex.kt

## Summary
File-based index that maps query handler method FQNs to their query parameter type FQNs. Mirrors `CqrsCommandHandlerIndex` but for the `@QueryHandler` attribute, enabling fast lookup from handler to query.

## Issues

### 1. [INDEX] Inconsistent version with CqrsQueryIndex
- Location: CqrsQueryHandlerIndex.kt:23
- Problem: `CqrsQueryHandlerIndex.getVersion()` returns `2`, while `CqrsQueryIndex.getVersion()` returns `1`. These indexes are semantically coupled, and the version should be synchronized.
- Why it matters: Stale indexes lead to inconsistent results and hard-to-debug issues in development. When one index is rebuilt and the other is not, query results diverge.
- Suggested fix: Ensure both `CqrsQueryHandlerIndex` and `CqrsQueryIndex` have the same version. Bump `CqrsQueryIndex` to version 2.

### 2. [BUG] Missing null safety with empty string fallback
- Location: CqrsQueryHandlerIndex.kt:38-45
- Problem: When the query parameter cannot be extracted, the code uses `?:` with an empty string (line 42). This produces an index entry with an empty-string key, which is semantically wrong: empty strings are meaningless keys and create ambiguous, unretrievable entries.
- Why it matters: An empty-string key pollutes the index and causes queries to return unrelated handlers. It also masks the real error: the handler could not be properly analyzed.
- Suggested fix: Skip indexing if the query type cannot be determined:
  ```kotlin
  override fun getIndexer() = DataIndexer<String, QueryHandlerType, FileContent> { inputData ->
      inputData
          .psiFile
          .let { PsiTreeUtil.findChildrenOfType(it, PhpAttribute::class.java) }
          .find { it.fqn == SpiralFrameworkClasses.CQRS_QUERY_HANDLER }
          .let { it?.owner as? Method }
          ?.let { method ->
              val query = method.getParameter(0)
                  .let { PsiTreeUtil.findChildrenOfType(it, ClassReference::class.java) }
                  .firstOrNull()
                  ?.fqn
              if (query != null) mapOf(query to method.fqn) else emptyMap()
          }
          ?: emptyMap()
  }
  ```

### 3. [STYLE] Commented-out code
- Location: CqrsQueryHandlerIndex.kt:46
- Problem: Line 46 contains commented-out code: `//.associate { it.first to it.second }`. Dead code should be removed.
- Why it matters: Commented code clutters the codebase and suggests incomplete refactoring.
- Suggested fix: Remove line 46.

### 4. [MAINTAINABILITY] Confusing type alias
- Location: CqrsQueryHandlerIndex.kt:16
- Problem: The type alias `QueryHandlerType = String` is confusing. The name suggests it represents a query handler type, but it actually represents the FQN of a handler method. The semantics are inverted.
- Why it matters: Code readers expect `QueryHandlerType` to relate to query types, not handler implementations. This leads to misunderstandings during maintenance.
- Suggested fix: Rename for clarity:
  ```kotlin
  private typealias HandlerMethodFqn = String
  class CqrsQueryHandlerIndex : AbstractIndex<HandlerMethodFqn>()
  ```

