# Analysis: php/patterns/AttributeFqnCondition.kt

## Summary
A PatternCondition subclass that matches PhpAttribute elements by their fully qualified name (FQN). It enables pattern-based matching of PHP attributes in PSI trees for use in reference contributors and completion patterns.

## Issues

### 1. [MAINTAINABILITY] Unused import and unused generic type parameter
- Location: AttributeFqnCondition.kt:8, 6
- Problem: The import `org.jetbrains.annotations.NonNls` on line 8 is declared but never used. Additionally, the generic type parameter `T : PhpAttribute` on line 10 is unnecessary because the `getPropertyValue()` method accepts `Any` and checks `is PhpAttribute` regardless, making the bound unused.
- Why it matters: Unused imports clutter the code and suggest incomplete refactoring. The type parameter adds false genericity without benefit.
- Suggested fix: Remove the unused `NonNls` import. Remove the generic parameter `<T : PhpAttribute>` and change the class signature to `PropertyPatternCondition<PhpAttribute?, String?>` or `PropertyPatternCondition<Any?, String?>` depending on intended use.

### 2. [API-MISUSE] Generic type parameter T is not actually enforced
- Location: AttributeFqnCondition.kt:10, 12
- Problem: The class is declared as `PropertyPatternCondition<T?, String?>` with `T : PhpAttribute`, but callers can instantiate it with any type (e.g., `AttributeFqnCondition<String>(pattern)`), and `getPropertyValue()` will silently return `null` for non-PhpAttribute objects. The type parameter provides no safety.
- Why it matters: Callers may pass incorrect generic arguments, and the code will silently fail to match instead of raising a compile-time error.
- Suggested fix: Remove the generic parameter and type the parent class as `PropertyPatternCondition<PhpAttribute?, String?>`. If polymorphism is needed, use `<T : PhpAttribute>` but validate in `getPropertyValue()` that `o` is actually of type `T` before returning null.

### 3. [STYLE] Missing KDoc comment
- Location: AttributeFqnCondition.kt:10-14
- Problem: No documentation explaining what this condition does or how it should be used in patterns.
- Why it matters: Developers using this class (from PsiReferenceContributor or pattern builders) may not understand that it filters attributes by FQN.
- Suggested fix: Add a KDoc comment:
  ```kotlin
  /**
   * A pattern condition that matches PhpAttribute elements by their fully qualified name (FQN).
   * Use with ElementPatterns.psiElement().withPattern(AttributeFqnCondition(...)) to filter attributes.
   */
  ```

## No-issue note
The core functionality (matching attributes by FQN) is sound. Issues are about type safety and documentation rather than behavioral bugs.
