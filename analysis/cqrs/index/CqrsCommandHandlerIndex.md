# Analysis: src/main/kotlin/com/github/xepozz/spiral/cqrs/index/CqrsCommandHandlerIndex.kt

## Summary
File-based index that maps handler method FQNs to their command parameter type FQNs. Indexes methods decorated with `@CommandHandler` attribute, extracting the first parameter's class reference as the index key.

## Issues

### 1. [INDEX] Inconsistent version between command and handler indexes
- Location: CqrsCommandHandlerIndex.kt:23
- Problem: `CqrsCommandHandlerIndex.getVersion()` returns `2`, while the corresponding `CqrsCommandIndex` returns `1` (lines 23 vs 23 in CqrsCommandIndex.kt). Both indexes are tightly coupled in the same indexing logic, so their versions should be synchronized.
- Why it matters: If one index is stale, both become inconsistent. When you bump the version of one, you must bump the other to invalidate old cache entries together.
- Suggested fix: Ensure both `CqrsCommandHandlerIndex` and `CqrsCommandIndex` have the same version number, and bump both if the indexing shape changes.

### 2. [BUG] Missing null safety on null command extraction
- Location: CqrsCommandHandlerIndex.kt:38-45
- Problem: When `it.getParameter(0)` fails or the class reference is not found, the code uses `?:` with an empty string (line 42). This produces an empty-string key in the index, which is semantically incorrect: an empty key means "handlers with unknown command type" but creates a useless and ambiguous entry.
- Why it matters: Queries for empty-string keys will return unrelated handlers. Debugging index corruption becomes difficult when stale entries with empty keys remain.
- Suggested fix: Skip indexing entirely if the command cannot be determined:
  ```kotlin
  override fun getIndexer() = DataIndexer<String, CommandHandlerType, FileContent> { inputData ->
      inputData
          .psiFile
          .let { PsiTreeUtil.findChildrenOfType(it, PhpAttribute::class.java) }
          .find { it.fqn == SpiralFrameworkClasses.CQRS_COMMAND_HANDLER }
          .let { it?.owner as? Method }
          ?.let {
              val command = it.getParameter(0)
                  .let { PsiTreeUtil.findChildrenOfType(it, ClassReference::class.java) }
                  .firstOrNull()
                  ?.fqn
              if (command != null) mapOf(command to it.fqn) else emptyMap()
          }
          ?: emptyMap()
  }
  ```

### 3. [STYLE] Commented-out code
- Location: CqrsCommandHandlerIndex.kt:46
- Problem: Line 46 contains commented-out code: `//.associate { it.first to it.second }`. Dead code should be removed.
- Why it matters: Commented code creates visual clutter, raises questions about whether this was debugging, and suggests incomplete refactoring.
- Suggested fix: Remove the comment on line 46.

### 4. [MAINTAINABILITY] Unclear type alias
- Location: CqrsCommandHandlerIndex.kt:16
- Problem: The type alias `CommandHandlerType = String` is defined but immediately shadowed by the generic parameter `<CommandHandlerType>()` in the class declaration. The name suggests "the type of a command handler" but it actually represents "the type of the indexed value" (handler method FQN). This is confusing.
- Why it matters: Future maintainers may think `CommandHandlerType` is a reference type instead of a string representation, leading to misunderstandings.
- Suggested fix: Rename to clarify intent:
  ```kotlin
  private typealias HandlerMethodFqn = String
  class CqrsCommandHandlerIndex : AbstractIndex<HandlerMethodFqn>()
  ```

