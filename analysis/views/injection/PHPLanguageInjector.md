# Analysis: src/main/kotlin/com/github/xepozz/spiral/views/injection/PHPLanguageInjector.kt

## Summary
This `MultiHostInjector` enables PHP code injection into Spiral template syntax (`{{ }}`, `{!! !!}`, and `@` directives) within XML and HTML contexts, allowing the editor to provide PHP syntax highlighting and completion.

## Issues

### 1. [STYLE] Banned logging pattern: println used instead of Logger
- Location: lines 51, 65, 78, 83
- Problem: The code uses `println(...)` for debug output instead of the proper `com.intellij.openapi.diagnostic.Logger`.
- Why it matters: Per CLAUDE.md, `println` is forbidden in new code. Debug logs should use the IntelliJ Platform logger.
- Suggested fix: Create a companion-object logger and replace all four `println` calls with `logger.debug(...)`. Alternatively, remove debug lines before publication.

### 2. [STYLE] Multiple commented-out debug statements
- Location: lines 29-31, 35-39, 51, 65
- Problem: Large blocks of commented-out code and debug logic clutter the file, including an incomplete conditional block and `println` statements.
- Why it matters: Commented-out code reduces maintainability and signals unfinished work.
- Suggested fix: Either complete the feature (e.g., the XmlAttribute filter at lines 29-31) or remove commented code. If the feature is intentionally disabled, document why with a comment.

### 3. [MAINTAINABILITY] Complex text range logic without documentation
- Location: lines 67-84
- Problem: The logic for computing `textRange` using two different code paths (single vs. multiple children) is complex and lacks documentation. The algorithm for finding opening/closing tags is non-obvious.
- Why it matters: Future maintainers will struggle to understand the intent or modify it safely.
- Suggested fix: Add a detailed comment explaining the algorithm and why two paths are needed. Consider extracting the logic into a helper method with a descriptive name.

### 4. [BUG] Potential variable shadowing and incorrect tag comparison
- Location: lines 73-74, 80-81
- Problem: Lines 73-74 declare and immediately reassign `openTag` and `closeTag` with `var` instead of `val`, making the initial assignment pointless. Additionally, line 81 compares `it.text` (a string) with `tagsMap[openTag.text]` (which expects `openTag` to be a `PsiElement`, not a `String`), creating a type mismatch.
- Why it matters: This is a likely bug that will prevent proper tag matching in multi-child scenarios.
- Suggested fix: Review the logic: should `openTag` and `closeTag` be strings (from `tagsMap.keys`) or `PsiElement`s? Fix the comparison accordingly to ensure type consistency.

## No further significant issues found.
