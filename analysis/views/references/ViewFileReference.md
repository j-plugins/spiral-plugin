# Analysis: src/main/kotlin/com/github/xepozz/spiral/views/references/ViewFileReference.kt

## Summary
This PSI reference resolves view file names to actual PHP files on disk. It supports both namespaced views (`namespace:filename`) and default views, constructing file paths and providing completion variants from filesystem directories.

## Issues

### 1. [MAINTAINABILITY] Missing null-coalescing for guessProjectDir
- Location: line 22
- Problem: The code calls `project.guessProjectDir()` and immediately returns `null` if it fails, but this is called in both `resolve()` and `getVariants()` (and similar patterns appear in `ViewNamespaceReference`).
- Why it matters: This is a minor code duplication that could be extracted into a helper method or validated at a higher level.
- Suggested fix: Consider extracting the `guessProjectDir()` check into a helper method or returning early at the reference contributor level if the project directory is not available.

### 2. [PERF] VirtualFileManager.findFileByNioPath called on main thread
- Location: lines 35, 54
- Problem: The code calls `VirtualFileManager.getInstance().findFileByNioPath(...)` in `resolve()` and `getVariants()` which may block if the file system is slow or if the VFS needs to refresh. This could occur on the EDT if called from a completion or navigation context.
- Why it matters: Blocking calls on EDT degrade IDE responsiveness.
- Suggested fix: If this is called during completion, defer VFS lookups using `ReadAction.nonBlocking(...)`. For `resolve()`, consider caching results using `CachedValuesManager`.

### 3. [STYLE] Hard-coded user-visible constant
- Location: line 84
- Problem: The `fileSuffix` constant uses string interpolation `".${SpiralViewUtil.VIEW_SUFFIX}"` to build the suffix. While not technically hard-coded, the prefix is implicit.
- Why it matters: This is minor, but for consistency, the suffix could be defined as a single constant with the dot included.
- Suggested fix: Define `const val FILE_SUFFIX = ".${SpiralViewUtil.VIEW_SUFFIX}"` or inline it directly.

## No further significant issues found.
