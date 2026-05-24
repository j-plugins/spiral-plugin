# Analysis: src/main/kotlin/com/github/xepozz/spiral/router/references/RouteReferenceContributor.kt

## Summary
Registers PSI reference providers for string literals within `@Route` attribute parameter lists, enabling navigation and completion for route URIs, names, methods, and groups.

## Issues

### 1. [MAINTAINABILITY] Complex and fragile PSI patterns
- Location: RouteReferenceContributor.kt:21-46, 48-72
- Problem: Two separate reference providers with overlapping but slightly different PSI patterns for handling route parameters. The first provider handles direct string arguments (lines 21-46), while the second handles strings inside array expressions (lines 48-72). Both are vulnerable to PSI structure changes.
- Why it matters: If the PSI structure of attribute parameter lists changes, both patterns must be updated simultaneously, increasing the risk of inconsistency or missed cases.
- Suggested fix: Consolidate the two patterns into a single provider using more flexible pattern matching, or document why two providers are necessary.

### 2. [MAINTAINABILITY] Hardcoded superparent depth
- Location: RouteReferenceContributor.kt:24-26, 50-56
- Problem: `withSuperParent(2, ...)` and `withSuperParent(4, ...)` use hardcoded parent traversal depths (2 and 4). If the PSI tree depth changes, these patterns will fail silently.
- Why it matters: Maintenance risk; future PSI changes will require updating the depth magic numbers without any indication of what they mean.
- Suggested fix: Add comments explaining the PSI hierarchy (e.g., "Parent 2: PhpAttribute (String -> ParamList -> Attribute)").

### 3. [PERF] No caching of argument lookups
- Location: RouteReferenceContributor.kt:30-44, 58-70
- Problem: Each reference provider calls `attribute.getPsiArgument(...)` multiple times to determine which reference type to create. For a single attribute, this involves multiple array searches through the arguments list.
- Why it matters: Redundant work on every completion/navigation invocation.
- Suggested fix: Cache the attribute argument mappings or reduce the number of comparisons by detecting argument position earlier.

### 4. [STYLE] Duplicate code between providers
- Location: RouteReferenceContributor.kt:29-44 and 57-70
- Problem: Both reference providers follow nearly identical logic: cast element, retrieve parent/attribute, then dispatch to reference types. The only difference is the PSI parent depth.
- Why it matters: Future changes to reference logic must be synchronized across both providers, risking divergence.
- Suggested fix: Extract common logic into a helper method that takes a parent extractor function.

### 5. [MAINTAINABILITY] Magic argument parameter positions
- Location: RouteReferenceContributor.kt:38-41, 67
- Problem: References are created by comparing the element with `attribute.getPsiArgument("uri", 0)`, `getPsiArgument("name", 1)`, etc. These correspond to the expected argument positions, but if the attribute signature changes, these comparisons will become invalid.
- Why it matters: Relies on hardcoded positions; changes to the attribute signature will silently disable references.
- Suggested fix: Add a comment documenting the expected `@Route` attribute signature and parameter positions.
