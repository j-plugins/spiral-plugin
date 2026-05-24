# Analysis: src/main/kotlin/com/github/xepozz/spiral/references/DirectoryReference.kt

## Summary
This reference class represents directory names used in the `directory()` function and resolves them to actual filesystem directories using the `SpiralViewUtil.PREDEFINED_DIRS` mapping. It provides IDE navigation to directories and completion variants for known directory shortcuts.

## Issues

### 1. [PERF] VirtualFileManager lookup on every resolve() call
- Location: lines 27
- Problem: `VirtualFileManager.getInstance().findFileByNioPath(path)` is called every time `resolve()` is invoked, without caching. For frequently accessed references (e.g., during code completion highlighting), this can trigger filesystem I/O repeatedly.
- Why it matters: Type providers and completion contributors call `resolve()` frequently, and filesystem I/O can block the IDE thread.
- Suggested fix: Consider caching the result of the resolve() call using `CachedValuesManager.getManager(project).getCachedValue(...)` with a dependency on the VFS root or project structure.

### 2. [THREAD] Potential EDT blocking on VFS I/O
- Location: lines 20-29
- Problem: `resolve()` performs synchronous I/O via `VirtualFileManager.getInstance().findFileByNioPath(path)`, which can block the EDT if called from an EDT context.
- Why it matters: Blocking I/O on the EDT freezes the IDE UI.
- Suggested fix: If this method is called from the EDT (e.g., during navigation), defer the I/O to a background thread or use `ReadAction.nonBlocking(...).submit(...)`. Alternatively, ensure callers handle the potential blocking correctly.

### 3. [API-MISUSE] calculateDefaultRangeInElement() double shifts the offset
- Location: lines 43-46
- Problem: The method calls `element.contentRange.shiftLeft(element.textOffset)` to calculate the range. However, `contentRange` is defined in `php/mixin.kt` as `ElementManipulators.getValueTextRange(this).shiftRight(textOffset)`, which already includes a `shiftRight(textOffset)`. The subsequent `shiftLeft(textOffset)` cancels out the `shiftRight()`, resulting in `ElementManipulators.getValueTextRange(this)`, which is already relative to the element's start. This is correct but confusing in the code.
- Why it matters: The calculation is unintentionally complex and makes it harder to verify correctness. Future maintainers may misunderstand what the code does.
- Suggested fix: Simplify by directly returning `ElementManipulators.getValueTextRange(this)`, or add a comment explaining why the shifts are necessary (they actually cancel out).

### 4. [STYLE] Unused import
- Location: (no import for `Path` at line 14 is unnecessary if only used for path construction)
- Problem: `kotlin.io.path.Path` is imported but there are alternative ways to construct paths (e.g., using `java.io.File` or string concatenation).
- Why it matters: Minor; this is a reasonable import for clarity.
- Suggested fix: No change strictly necessary; the import is acceptable.

### 5. [MAINTAINABILITY] Hard-coded path separator
- Location: line 25
- Problem: The path is constructed with `"${projectDir.path}/$offset"`, which uses a hard-coded `/` separator. On Windows, `VirtualFileManager.findFileByNioPath()` expects forward slashes, but mixing `projectDir.path` (which may use backslashes) with `/` could cause issues.
- Why it matters: Path construction may be platform-specific and could fail on Windows.
- Suggested fix: Use `Path` (from `kotlin.io.path`) to handle path separators correctly: `val path = Path(projectDir.path).resolve(offset.trim('/'))` instead of string concatenation.

### 6. [DUMB-MODE] No explicit handling in resolve()
- Location: lines 20-29
- Problem: `resolve()` does not check `DumbService.isDumb(project)`. While VFS lookups are not typically forbidden in dumb mode, it's good practice to fail fast if the IDE is indexing.
- Why it matters: During indexing, VFS may be inconsistent; this could lead to stale directory references being returned.
- Suggested fix: Consider adding an early check: `if (DumbService.isDumb(project)) return null`.
