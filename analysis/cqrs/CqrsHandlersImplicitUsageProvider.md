# Analysis: src/main/kotlin/com/github/xepozz/spiral/cqrs/CqrsHandlersImplicitUsageProvider.kt

## Summary
Marks CQRS command and query handler classes as implicitly used, preventing IDE warnings when these classes are registered via attributes but not directly referenced in code. Correctly implements the `ImplicitUsageProvider` extension point.

## Issues

### 1. [STYLE] Inconsistent handling of missing parameter
- Location: CqrsHandlersImplicitUsageProvider.kt:30
- Problem: `isClassWithCustomizedInitialization()` returns `true` unconditionally for all elements, regardless of whether they are CQRS handlers. This method should match the logic in `isImplicitUsage()`.
- Why it matters: The method signals to the IDE that a class has custom initialization logic, which is only true for CQRS handler classes. Returning `true` for all PSI elements could mislead IDE analysis and cause unnecessary suppression of warnings.
- Suggested fix: Change line 30 to apply the same CQRS handler detection logic:
  ```kotlin
  override fun isClassWithCustomizedInitialization(element: PsiElement) = isImplicitUsage(element)
  ```

