# Analysis: src/main/kotlin/com/github/xepozz/spiral/prototyped/PrototypedCompletion.kt

## Summary
This completion contributor provides IDE code completion for prototype properties on classes that use the `PrototypeTrait`. It queries the `PrototypedIndex` and presents all indexed prototype names as completion options.

## Issues

### 1. [DUMB-MODE] No dumb-mode guard on index access
- Location: lines 46-53
- Problem: `PrototypedIndex.getAll(project)` calls `FileBasedIndex.getInstance().getValues(...)` and `getAllKeys(...)` without checking `DumbService.isDumb(project)`. According to the IntelliJ Platform best-practices checklist, `FileBasedIndex` queries are forbidden in dumb mode.
- Why it matters: During indexing (dumb mode), the index may be incomplete or inconsistent, leading to missing or incorrect completion suggestions. The IDE may crash or produce wrong results.
- Suggested fix: Add a guard at the start of `addCompletions`: `if (DumbService.isDumb(project)) return`. Alternatively, use `DumbService.runReadActionInSmartMode { ... }`.

### 2. [PERF] Missing result.isStopped() check in loop
- Location: lines 48-52
- Problem: The completion loop iterating over `PrototypedIndex.getAll(project)` does not check `result.isStopped()` between iterations.
- Why it matters: If the user cancels the completion request, the contributor will still process all remaining items unnecessarily, wasting CPU and delaying responsiveness.
- Suggested fix: Add `if (result.isStopped()) return` after the loop, or check it inside the loop for very large index results. Call `ProgressManager.checkCanceled()` in long loops.

### 3. [API-MISUSE] Unused import FieldReference
- Location: line 18
- Problem: `FieldReference` is imported but never used.
- Why it matters: Unnecessary imports reduce code clarity.
- Suggested fix: Remove the unused import.

### 4. [MAINTAINABILITY] Magic pattern specificity
- Location: lines 24-32
- Problem: The pattern matches `psiElement().withParent(FieldReference).withFirstChild(Variable.withName("this"))`. This is already fairly specific, but there is no explicit check to ensure we are only completing when the field name is empty (i.e., at the position where the user is typing the field name after `$this->|`).
- Why it matters: If the pattern matches more broadly than intended, the contributor may offer completions in unexpected contexts.
- Suggested fix: Consider adding an explicit check in `addCompletions` to verify the element is at a valid completion position (e.g., after `->` and at a point where a new identifier would be expected). The current pattern should be adequate, but could be documented.

### 5. [STYLE] No early return for safety
- Location: line 40
- Problem: The cast `val element = parameters.position.parent as? FieldReference ?: return` is correct, but could be made clearer by storing the result earlier.
- Why it matters: Minor code clarity issue; the current code is acceptable but could be improved for readability.
- Suggested fix: This is a minor point; no change strictly necessary.
