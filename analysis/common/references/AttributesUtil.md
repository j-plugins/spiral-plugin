# Analysis: common/references/AttributesUtil.kt

## Summary
A utility singleton that extracts PSI argument values from PhpAttribute objects by name or positional index. It handles both named and positional argument lookup, falling back from named to indexed lookups.

## Issues

### 1. [MAINTAINABILITY] Confusing fallback logic with redundant index calculation
- Location: AttributesUtil.kt:7-13
- Problem: The function has two different fallback paths: (1) try named argument, fall back to positional by index, (2) then extract the argument index from the matched argument and use that for parameter lookup instead. This creates subtle behavior where the final `index` parameter passed in may be ignored if an argument is found. The variable `argumentIndex` is computed but may not reflect the caller's intent.
- Why it matters: A caller passing `index=0` expecting the first parameter may get a different parameter if an argument with a non-zero `argumentIndex` exists. The logic is non-obvious.
- Suggested fix: Clarify the intent in a doc comment. For example: "Get the parameter corresponding to the given argument. Looks up by name first, falls back to positional index. Returns the parameter index of the matched argument if found." Alternatively, simplify to always use the caller's `index` if a positional fallback is needed, or always use the argument's internal index.

### 2. [API-MISUSE] Potential null dereference on argument property
- Location: AttributesUtil.kt:10
- Problem: `argument?.argument?.argumentIndex` dereferences `argument?.argument` without null-safety. If `argument` is non-null but `argument.argument` is null, this will NPE. The Elvis operator `?: index` at the end masks the issue.
- Why it matters: NPE at this line is possible if the PSI structure is incomplete. Better to be explicit.
- Suggested fix: Use explicit null-coalescing: `val argumentIndex = (argument?.argument?.argumentIndex) ?: index` or unpack the chain safely.

## No-issue note
The core logic is reasonable for PHP attribute argument extraction. Both issues are about clarity and safety rather than correctness bugs in typical cases.
