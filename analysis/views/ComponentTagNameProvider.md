# Analysis: src/main/kotlin/com/github/xepozz/spiral/views/ComponentTagNameProvider.kt

## Summary
This `XmlTagNameProvider` contributes component tag name suggestions to the XML editor. It searches for files matching the pattern `x-*.dark.php` and offers them as lookup completions for component tags.

## Issues

### 1. [PERF] Unbounded filename index scan without dumb mode guard
- Location: lines 21-31
- Problem: The code calls `FilenameIndex.processAllFileNames(...)` in `addTagNameVariants(...)`, which is a completion contributor that may be invoked during indexing (dumb mode). There is no guard against dumb mode.
- Why it matters: Scanning all filenames in dumb mode blocks indexing and degrades IDE responsiveness. Per the checklist, file-based operations must be guarded with `DumbService.isDumb()` checks.
- Suggested fix: Wrap the `FilenameIndex.processAllFileNames(...)` call in a `DumbService.isDumb(project)` check; return early if dumb, or defer to smart mode using `DumbService.getInstance(project).runWhenSmart(...)`.

### 2. [STYLE] Hard-coded user-visible string
- Location: line 38
- Problem: The type text `"Spiral Component"` is a hard-coded string instead of being localized via `SpiralBundle.message(...)`.
- Why it matters: Per CLAUDE.md, all user-visible strings must use `SpiralBundle.message(...)` for i18n support.
- Suggested fix: Add a key to `SpiralBundle.properties` (e.g., `spiral.component.type=Spiral Component`) and use `SpiralBundle.message("spiral.component.type")`.

## No further significant issues found.
