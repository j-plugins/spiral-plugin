# Analysis: common/references/InsertTextInsertHandler.kt

## Summary
A completion insert handler that conditionally applies text insertion based on whether the text is already present at the cursor position. It extends DeclarativeInsertHandler and uses a helper method to check for duplicate content before insertion.

## Issues

### 1. [PERF] Redundant text lookup without offset bounds checking
- Location: InsertTextInsertHandler.kt:25-30
- Problem: `isValueAlreadyHere()` constructs a TextRange without verifying that `startOffset + valueLength` is within bounds before calling `document.getText()`. The bounds check on line 28 uses `>=` which is correct, but if the document is exactly at the boundary (e.g., caret at end of document), `TextRange.create(startOffset, startOffset + valueLength)` may request text beyond the document end.
- Why it matters: Calling `getText()` with an out-of-bounds range may throw an exception or return incorrect results depending on IntelliJ API version. Also, this check happens on every insertion, adding unnecessary overhead.
- Suggested fix: Ensure the range is valid before calling `getText()`: `if (startOffset + valueLength > editor.document.textLength) return false;` earlier in the function. Or rely on `getText()` throwing and catch it gracefully.

### 2. [MAINTAINABILITY] Unclear relationship between DeclarativeInsertHandler and conditionalHandleInsert
- Location: InsertTextInsertHandler.kt:12-17, 22
- Problem: The class inherits from `DeclarativeInsertHandler` and passes a `RelativeTextEdit` at construction, but then overrides `handleInsert()` and calls `conditionalHandleInsert()` with a boolean flag. The relationship between the declarative edit and the conditional application is unclear. Is `RelativeTextEdit` ever used, or does `conditionalHandleInsert()` always apply it?
- Why it matters: Code reviewers and future maintainers may be confused about the actual insertion flow and may accidentally break the logic when refactoring.
- Suggested fix: Add a KDoc comment explaining why DeclarativeInsertHandler is extended and how `conditionalHandleInsert()` interacts with the declarative edits. Document the contract of `applyTextOperations` flag.

## No-issue note
The core functionality (deduplicate text before inserting) is sound for completion contexts where the desired text may already exist.
