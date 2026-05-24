# Analysis: src/main/kotlin/com/github/xepozz/spiral/cqrs/index/CqrsCommandIndex.kt

## Summary
File-based index that maps command class FQNs to their handler method FQNs. Reverses the indexing of `CqrsCommandHandlerIndex` to enable fast lookup: given a command interface, find all handlers.

## Issues

### 1. [INDEX] Inconsistent version with CqrsCommandHandlerIndex
- Location: CqrsCommandIndex.kt:23
- Problem: `CqrsCommandIndex.getVersion()` returns `1`, while `CqrsCommandHandlerIndex.getVersion()` returns `2`. These indexes are semantically linked (one is the reverse mapping of the other), so they should be kept in sync.
- Why it matters: Version mismatch means one index may be rebuilt while the other is stale, leading to inconsistent query results and hard-to-debug bugs.
- Suggested fix: Ensure both indexes have the same version number. Since `CqrsCommandHandlerIndex` is at version 2, bump `CqrsCommandIndex` to 2 as well.

### 2. [BUG] Unsafe conversion from null to empty string
- Location: CqrsCommandIndex.kt:38-45
- Problem: When the command parameter cannot be extracted (line 42), the code returns `null` via `return@let null` inside the nested `let` (line 42). However, if extraction succeeds, the outer code maps `command to it.fqn` (line 43). The index key (the command) could be `null` if extraction fails, which is then implicitly treated as a valid key. The logic is: if extraction fails, return `null` from the let, which causes the outer `?.let` to return `emptyMap()`. But the intent and flow are unclear due to nested `let` blocks.
- Why it matters: The current code is fragile and hard to follow. If the `return@let null` is removed or modified, the code could silently index handlers with `null` keys or skip them incorrectly.
- Suggested fix: Clarify the logic:
  ```kotlin
  override fun getIndexer() = DataIndexer<String, CommandType, FileContent> { inputData ->
      inputData
          .psiFile
          .let { PsiTreeUtil.findChildrenOfType(it, PhpAttribute::class.java) }
          .find { it.fqn == SpiralFrameworkClasses.CQRS_COMMAND_HANDLER }
          .let { it?.owner as? Method }
          ?.let { method ->
              val command = method.getParameter(0)
                  .let { PsiTreeUtil.findChildrenOfType(it, ClassReference::class.java) }
                  .firstOrNull()
                  ?.fqn
              if (command != null) mapOf(command to method.fqn) else emptyMap()
          }
          ?: emptyMap()
  }
  ```

### 3. [STYLE] Commented-out code
- Location: CqrsCommandIndex.kt:46
- Problem: Line 46 contains commented-out code: `//.associate { it.first to it.second }`. This is dead code left over from refactoring.
- Why it matters: Commented code creates confusion and suggests incomplete cleanup.
- Suggested fix: Remove line 46.

### 4. [MAINTAINABILITY] Unclear type alias
- Location: CqrsCommandIndex.kt:16
- Problem: The type alias `CommandType = String` suggests a reference to a command type, but actually represents the FQN string of a handler method. The semantics are reversed compared to the variable name.
- Why it matters: Confusing naming slows down code review and maintenance. Readers expect `CommandType` to be related to commands, not handlers.
- Suggested fix: Rename for clarity:
  ```kotlin
  private typealias HandlerMethodFqn = String
  class CqrsCommandIndex : AbstractIndex<HandlerMethodFqn>()
  ```

