# Analysis: src/main/kotlin/com/github/xepozz/spiral/prototyped/PrototypedAttributeReferenceContributor.kt

## Summary
This contributor registers PSI references for string literals inside `@Prototyped` attribute parameter lists, creating self-references to the containing PHP class. It enables IDE navigation and completion for the attribute.

## Issues

### 1. [STYLE] Commented-out debug println
- Location: line 41
- Problem: `println("reference: $element: ${element.text}")` is commented out but still present in the code.
- Why it matters: Per CLAUDE.md conventions, no `println` statements should remain in new code. Commented-out debug code clutters the codebase.
- Suggested fix: Remove the commented line entirely.

### 2. [API-MISUSE] Unused import PsiElementRef
- Location: line 9
- Problem: `PsiElementRef` is imported but never used in the file.
- Why it matters: Unnecessary imports reduce code clarity and may suggest incomplete refactoring.
- Suggested fix: Remove the unused import.

### 3. [API-MISUSE] Unused import PsiElementResult
- Location: line 5
- Problem: `PsiElementResult` is imported but never used in the file.
- Why it matters: Unnecessary imports reduce code clarity.
- Suggested fix: Remove the unused import.

### 4. [API-MISUSE] Unused import PsiReferenceWrapper
- Location: line 16
- Problem: `PsiReferenceWrapper` is imported but never used in the file.
- Why it matters: Unnecessary imports reduce code clarity.
- Suggested fix: Remove the unused import.

### 5. [MAINTAINABILITY] Missing getRangeInElement() implementation
- Location: class PrototypedAttributeReferenceContributor, method getReferencesByElement (lines 36-50)
- Problem: The returned `PsiReferenceBase` created via `createSelfReference` may not have the correct range for the string literal content. Self-references typically resolve to the whole element, but the IDE may expect the range to point to the actual string content within quotes.
- Why it matters: This could cause incorrect navigation or incorrect visual range highlighting in the IDE during reference resolution.
- Suggested fix: Consider creating a custom PsiReferenceBase subclass that overrides `getRangeInElement()` to return the content range using the `contentRange` helper from `php/mixin.kt`, similar to how `DirectoryReference` does it.

### 6. [DUMB-MODE] No dumb-mode guard
- Location: lines 43-45
- Problem: The contributor may be called during indexing (dumb mode) when PSI tree is not fully reliable. There is no guard for `DumbService.isDumb(project)`.
- Why it matters: In dumb mode, `PsiTreeUtil.getParentOfType(element, PhpClass::class.java)` may return null or stale results, leading to incomplete or incorrect reference creation.
- Suggested fix: Add an early return if in dumb mode, or use `DumbService.isDumb(project)` check before PSI traversal.
