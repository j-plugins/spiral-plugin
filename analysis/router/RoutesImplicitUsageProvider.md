# Analysis: src/main/kotlin/com/github/xepozz/spiral/router/RoutesImplicitUsageProvider.kt

## Summary
Declares classes with `@Route` attributes as implicitly used, preventing IDE warnings when the class is not explicitly referenced in code. Implements `ImplicitUsageProvider` to integrate with the IDE's inspection framework.

## Issues

### 1. [PERF] Inefficient PSI traversal on every invocation
- Location: RoutesImplicitUsageProvider.kt:12-14
- Problem: `isImplicitUsage` iterates through all `PhpAttribute` children of every class element checked, which includes non-Route attributes. This traversal happens for every class in the IDE's inspection system.
- Why it matters: Can cause noticeable slowdown when inspecting large classes with many attributes or during batch inspections.
- Suggested fix: First check if element is a `PhpClass`, then use early exit or caching. Consider using `CachedValuesManager` to cache results per class.

### 2. [STYLE] Unused return value in override methods
- Location: RoutesImplicitUsageProvider.kt:19-21
- Problem: `isImplicitRead` and `isImplicitWrite` always return `false` but override abstract methods from `ImplicitUsageProvider`. While this is correct (routes are written by the framework, not read/written in code), the pattern is clearer with explicit comments.
- Why it matters: Minor clarity issue, but new readers may not understand why these methods return hardcoded `false`.
- Suggested fix: Add brief comments above `isImplicitRead` and `isImplicitWrite` explaining that routes are runtime-only.

### 3. [MAINTAINABILITY] Unused return value in `isClassWithCustomizedInitialization`
- Location: RoutesImplicitUsageProvider.kt:23
- Problem: `isClassWithCustomizedInitialization` always returns `true` unconditionally. Per the IntelliJ Platform API contract, returning `true` here means "this class has custom initialization logic beyond field assignment," but it's unclear if routed classes actually fit this description.
- Why it matters: If misconfigured, could prevent legitimate inspection warnings on actual initialization issues in route controller classes.
- Suggested fix: Either return `false` (routes don't have special initialization) or add a comment explaining the intention. Check Spiral framework documentation to confirm if route controller classes require custom initialization detection.

## No additional issues found.
