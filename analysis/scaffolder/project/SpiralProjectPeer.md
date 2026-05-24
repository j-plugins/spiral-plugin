# Analysis: src/main/kotlin/com/github/xepozz/spiral/scaffolder/project/SpiralProjectPeer.kt

## Summary
This class provides the UI for the Spiral project generator settings (template selection, version, Git initialization). It fetches available package versions asynchronously and binds the UI to settings state.

## Issues

### 1. [STYLE] Hard-coded user-facing label strings
- Location: lines 25, 29, 33
- Problem: The labels `"Template"`, `"Version"`, and `"Create Git"` are hard-coded strings instead of being localized via `SpiralBundle.message(...)`.
- Why it matters: Per CLAUDE.md, all user-visible strings must use `SpiralBundle.message(...)` for i18n support.
- Suggested fix: Add keys to `SpiralBundle.properties` (e.g., `spiral.project.peer.template=Template`, `spiral.project.peer.version=Version`, `spiral.project.peer.createGit=Create Git`) and return `SpiralBundle.message(...)` for each label.

### 2. [STYLE] Hard-coded template package name
- Location: line 26
- Problem: The template list is a hard-coded single item `listOf("spiral/app")` instead of being externalizable.
- Why it matters: This couples the UI to a fixed template; if templates change, code must be modified.
- Suggested fix: Consider moving the default template to `SpiralProjectGeneratorSettings` or a constant, or externalize it to configuration.

## No further significant issues found.
