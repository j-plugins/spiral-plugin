# Analysis: common/index/AbstractIndex.kt

## Summary
This is a minimal abstract base class for file-based indexes used across the plugin. It provides default implementations for key descriptor, content dependency, and version handling, requiring concrete subclasses to implement name, input filter, and the indexer itself.

## Issues

### 1. [INDEX] Missing version override guidance in subclasses
- Location: AbstractIndex.kt:21
- Problem: The class defines `getVersion() = 1` as a static default, but the CLAUDE.md documentation explicitly states "Bump `getVersion()` in the concrete index whenever you change the indexer output shape." This base implementation provides no mechanism or comment to enforce version bumping, and subclasses may inherit the version value without realizing it needs to change.
- Why it matters: File-based index version mismatches cause stale caches to corrupt results. Silently inheriting version 1 across all subclasses means any index logic change risks silent data corruption.
- Suggested fix: Mark `getVersion()` as `open` and add a KDoc comment above it warning subclasses to override and increment when the indexer output shape changes (keys, value type, input filter).

### 2. [MAINTAINABILITY] Ambiguous default for dependsOnFileContent
- Location: AbstractIndex.kt:15
- Problem: `dependsOnFileContent() = true` is hardcoded as the default. Per the CLAUDE.md conventions, "path-only indexes should override" this. However, there is no documentation at the class or method level indicating which indexes should or should not depend on file content.
- Why it matters: Path-only indexes (e.g., detecting file names without examining content) will inefficiently rebuild on every file content change. Developers may not realize they need to override.
- Suggested fix: Add a KDoc comment explaining that content-dependent indexes can use the default, but path-only indexes must override with `override fun dependsOnFileContent() = false`.

## No-issue note
The core design is sound for a minimal base class. Both issues are about improving guidance rather than structural flaws.
