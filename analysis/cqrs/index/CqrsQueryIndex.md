# Analysis: src/main/kotlin/com/github/xepozz/spiral/cqrs/index/CqrsQueryIndex.kt

## Summary
File-based index that maps query class FQNs to their handler method FQNs. Reverses `CqrsQueryHandlerIndex` to enable fast lookup: given a query interface, find all handlers.

## Issues

### 1. [INDEX] Inconsistent version with CqrsQueryHandlerIndex
- Location: CqrsQueryIndex.kt:23
- Problem: `CqrsQueryIndex.getVersion()` returns `1`, while `CqrsQueryHandlerIndex.getVersion()` returns `2`. These indexes are semantically linked and should be versioned together.
- Why it matters: Version skew causes one index to become stale while the other is fresh, producing inconsistent results.
- Suggested fix: Bump `CqrsQueryIndex` version to 2 to match `CqrsQueryHandlerIndex`.

### 2. [BUG] Unsafe null handling in nested let blocks
- Location: CqrsQueryIndex.kt:38-45
- Problem: The code uses nested `let` blocks with `return@let null` (line 42). The flow is: extract the query parameter type, and if it cannot be found, return `null` from the inner `let`, which causes the outer `?.let` to return `emptyMap()`. However, the logic is unclear and fragile. If someone modifies the return statement or removes it, the code could silently index with `null` keys.
- Why it matters: This pattern is error-prone and hard to follow. Refactoring could easily introduce bugs where null values get indexed.
- Suggested fix: Clarify the logic with explicit null checks:
  ```kotlin
  override fun getIndexer() = DataIndexer<String, QueryType, FileContent> { inputData ->
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
- Location: CqrsQueryIndex.kt:46
- Problem: Line 46 contains commented-out code: `//.associate { it.first to it.second }`. This should be removed.
- Why it matters: Commented code creates confusion and suggests incomplete refactoring work.
- Suggested fix: Remove line 46.

### 4. [MAINTAINABILITY] Misleading type alias
- Location: CqrsQueryIndex.kt:16
- Problem: The type alias `QueryType = String` suggests it represents a query type reference, but it actually represents the FQN string of a handler method. The naming is semantically reversed.
- Why it matters: Code readers expect `QueryType` to be related to queries, not handler implementations. This confuses the index semantics.
- Suggested fix: Rename for clarity:
  ```kotlin
  private typealias HandlerMethodFqn = String
  class CqrsQueryIndex : AbstractIndex<HandlerMethodFqn>()
  ```

