# Analysis: src/main/kotlin/com/github/xepozz/spiral/router/references/RouterMethodsReference.kt

## Summary
Provides code completion for HTTP method names (GET, POST, PUT, etc.) in `@Route` attributes. A soft reference that sources methods from `RouterIndexUtil.ALL_VERBS`.

## Issues

### 1. [MAINTAINABILITY] No dumb mode check
- Location: RouterMethodsReference.kt:11-30
- Problem: While `getVariants()` doesn't directly query the index (it uses the hardcoded `ALL_VERBS` list), this reference is still created by `RouteReferenceContributor` which runs during PSI traversal, including during indexing. No explicit dumb-mode handling exists at this level.
- Why it matters: Reference creation is invoked during indexing, though the reference itself is safe. However, consistency with other reference types suggests dumb-mode awareness.
- Suggested fix: No fix required; document that this reference is dumb-mode safe because it doesn't query the index.

### 2. [STYLE] Inconsistent lookup string casing
- Location: RouterMethodsReference.kt:25
- Problem: `withLookupString(it.lowercase())` provides lowercase lookup, but the `LookupElementBuilder.create(it)` uses the uppercase method name for display. This allows case-insensitive completion but displays uppercase.
- Why it matters: Minor UX inconsistency; users typing "get" will match but the suggestion shows "GET". This is acceptable but could be more consistent.
- Suggested fix: Document this behavior with a comment, or consider standardizing to lowercase display if HTTP methods are conventionally shown lowercase in the IDE.
