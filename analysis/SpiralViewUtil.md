# Analysis: src/main/kotlin/com/github/xepozz/spiral/SpiralViewUtil.kt

## Summary
Utility object defining view-related constants: the `.dark.php` file suffix for Spiral views and a map of predefined directory aliases (public, vendor, runtime, etc.) used for resolving directory references in completion and navigation.

## Issues

### 1. [STYLE] PREDEFINED_DIRS should be immutable (val with unmodifiable Map)
- Location: SpiralViewUtil.kt:6
- Problem: `PREDEFINED_DIRS` is declared as a mutable `mapOf(...)` result, but `mapOf()` returns an immutable `Map`. However, the declaration reads as if it could be modified. The type is implicitly `Map<String, String>`, which is correct, but clarity could be improved.
- Why it matters: While technically immutable, the absence of an explicit immutability annotation (like `val PREDEFINED_DIRS: Map<String, String> =`) leaves reader uncertainty about mutability intent.
- Suggested fix: Add an explicit type annotation to clarify immutability: `val PREDEFINED_DIRS: Map<String, String> = mapOf(...)`. This makes the immutability contract explicit and aids IDE inference.

No other significant issues found.

