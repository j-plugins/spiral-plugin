# Analysis: src/main/kotlin/com/github/xepozz/spiral/forms/FilterAttributesImplicitUsageProvider.kt

## Summary
Marks all fields in classes extending `AttributesFilter` as implicitly used / implicitly written so they are not flagged by IDE inspections.

## Issues

### 1. [STYLE] Commented-out debug `println`
- Location: FilterAttributesImplicitUsageProvider.kt:17
- Problem: Dead `// ... println(...)` line.
- Suggested fix: Delete the comment.

### 2. [MAINTAINABILITY] Asymmetric `isImplicitRead` vs `isImplicitWrite`
- Location: FilterAttributesImplicitUsageProvider.kt:19, 21
- Problem: `isImplicitRead()` returns `false` while `isImplicitWrite()` returns `true`.
- Why it matters: Filter fields are read by the framework (when building the validated DTO), not just written. Returning `false` for read leaves "Unused field" warnings on fields that are actually consumed.
- Suggested fix: Implement `isImplicitRead` symmetrically: return `true` for fields whose containing class extends `AttributesFilter`.
