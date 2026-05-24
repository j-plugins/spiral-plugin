# Analysis: src/main/kotlin/com/github/xepozz/spiral/config/env/EnvFoldingBuilder.kt

## Summary
Implements code folding for PHP `env()` function calls, collapsing them to display the parameter name inline.

## Issues

### 1. [API-MISUSE] Hardcoded FQN string should use SpiralFrameworkClasses constant
- Location: EnvFoldingBuilder.kt:33
- Problem: The hardcoded FQN `"\\env"` is checked directly instead of being defined as a constant in `SpiralFrameworkClasses`.
- Why it matters: Violates the project convention (CLAUDE.md) that all Spiral/PHP framework FQNs must live in `SpiralFrameworkClasses.kt`.
- Suggested fix: Add `const val ENV_FUNCTION = "\\env"` to `SpiralFrameworkClasses`, then `if (it.fqn != SpiralFrameworkClasses.ENV_FUNCTION)`.

### 2. [MAINTAINABILITY] No null-safety on `it.parameters[0]`
- Location: EnvFoldingBuilder.kt:37
- Problem: After checking `it.parameters.size < 1`, code accesses `it.parameters[0]` directly. Off-by-one: `size < 1` allows size==0 only? The bound is correct (size<1 means 0), but `getOrNull(0)` is safer.
- Suggested fix: `it.parameters.getOrNull(0)?.text?.let { p -> "env: $p" } ?: "env"`.
