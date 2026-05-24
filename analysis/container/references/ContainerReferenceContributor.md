# Analysis: src/main/kotlin/com/github/xepozz/spiral/container/references/ContainerReferenceContributor.kt

## Summary
PSI reference contributor recognizing `new \Spiral\Core\Container\Autowire(SomeClass::class, [...])` patterns and providing references for the array keys to constructor parameter names.

## Issues

### 1. [API-MISUSE] Hardcoded Autowire FQN
- Location: ContainerReferenceContributor.kt:41
- Problem: FQN `"\\Spiral\\Core\\Container\\Autowire"` is hardcoded instead of being a constant in `SpiralFrameworkClasses`.
- Why it matters: Violates CLAUDE.md project convention.
- Suggested fix: Add `const val AUTOWIRE = "\\Spiral\\Core\\Container\\Autowire"` to `SpiralFrameworkClasses` and reference it.

### 2. [STYLE] Commented `println`
- Location: ContainerReferenceContributor.kt:49
- Problem: `// println(...)` debug line.
- Suggested fix: Delete.

### 3. [MAINTAINABILITY] Deep pattern nesting hard to verify
- Location: ContainerReferenceContributor.kt:19-29
- Problem: 5-level `withSuperParent()` chain without documentation.
- Suggested fix: Add a single-line comment describing the parent chain (`StringLiteral → ArrayHashElement → ArrayCreation → ParameterList → NewExpression`).
