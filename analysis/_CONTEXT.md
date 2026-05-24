# Shared analysis context — Spiral Framework plugin

This file is shared context for per-file analysis subagents.

## Project facts (from CLAUDE.md)
- Language: Kotlin (JVM toolchain 21). IntelliJ Platform Gradle Plugin 2.x.
- Plugin ID: `com.github.xepozz.spiral`. Mandatory deps: `com.intellij.modules.platform`, `com.jetbrains.php`.
- Since-build: 251 (IDE 2025.1+). `platformType = IU`. PhpStorm-only APIs from `com.jetbrains.php` are available.
- Tests: JUnit 4 + `BasePlatformTestCase`.
- All Spiral FQNs MUST live in `SpiralFrameworkClasses.kt` with leading backslash (`\Spiral\...`).
- All user-facing strings MUST go through `SpiralBundle.message(...)`.
- Icons via `SpiralIcons` only.
- No `println` in new code — use `com.intellij.openapi.diagnostic.Logger`.
- Indexes extend `common/index/AbstractIndex.kt`, must bump `getVersion()` when output shape changes.
- `ObjectStreamDataExternalizer` uses Java serialization — value classes must stay `Serializable`.
- Configuration cache enabled — don't break it.

## IntelliJ Platform best-practices checklist (use as a lens when reviewing)

### Threading & locks
- Long PSI traversal, VFS scans, index queries must NOT run on EDT.
- Reading PSI/VFS requires a read action (or smart-mode read access). Writing requires a write action on EDT.
- Re-validate PSI / VirtualFile (`isValid()`) when re-entering a read action after suspension.
- `ProcessCanceledException` must NEVER be swallowed — always rethrow. Catch only specific exceptions.
- Avoid `runReadAction` on EDT for heavy work — prefer `ReadAction.nonBlocking(...).submit(...)` or background actions.

### Dumb mode & indexes
- `FileBasedIndex.getInstance().getValues(...)` / `getAllKeys(...)` is forbidden in dumb mode — guard with `DumbService.isDumb` or `runReadActionInSmartMode`.
- `LineMarkerProvider`, `ReferenceContributor`, `CompletionContributor` may be invoked during indexing — handle dumb mode.
- Concrete `FileBasedIndexExtension` must bump `getVersion()` if the indexed shape changes (keys, value class, input filter, key descriptor).
- `dependsOnFileContent()` defaults to true in `AbstractIndex` — fine for content indexes, but path-only indexes should override.
- Value externalizer using `ObjectStreamDataExternalizer` (Java serialization) is fragile across IDE/plugin versions — prefer `DataExternalizer<T>` with explicit `save`/`read`.

### PSI references
- `PsiReference.resolve()` must be fast — cache where possible, use `CachedValuesManager` for repeated resolves.
- `PsiReferenceContributor` patterns should be specific to avoid invocation on every PSI element.
- `getRangeInElement()` must be relative to the host element (not absolute).
- Multi-resolve references should implement `PsiPolyVariantReference`.
- `getVariants()` returning many items: use `LookupElementBuilder` and consider `withIcon`, `withTypeText`, `withTailText` for UX.

### Completion contributors
- `addCompletions` must be fast; avoid blocking I/O.
- Always check `result.isStopped`/`ProgressManager.checkCanceled()` in long loops.
- `extend(...)` patterns should be as specific as possible (use `psiElement().withParent(...)`).
- Don't call `result.stopHere()` unless intentionally suppressing other contributors.

### LineMarkerProvider
- `LineMarkerProvider.getLineMarkerInfo` is invoked on the leaf element (not the whole class/function) — anchor to identifier, not container.
- `RelatedItemLineMarkerProvider` is the modern alternative.
- Marker must be cheap — heavy resolution should be deferred via `NavigationGutterIconBuilder`.

### Run configurations
- `ConfigurationFactory.getId()` must be stable across versions — changing it breaks user-saved configs.
- `RunConfiguration.checkConfiguration()` should throw `RuntimeConfigurationError` / `RuntimeConfigurationWarning`.
- `RunProfileState.execute()` should not block EDT.
- Read/write external state via `JDOMExternalizer` / explicit XML; `Element` content is persisted to workspace.xml.

### Service lifecycle / Disposers
- Never use `Application` or `Project` directly as a Disposable parent inside plugin code — use the plugin's service.
- Listeners registered without a `Disposable` parent leak across dynamic-plugin reloads.
- Project services should be `@Service(Service.Level.PROJECT)`; app services `@Service(Service.Level.APP)`.

### Other quality concerns
- Hard-coded user-visible strings (must be in `SpiralBundle`).
- Hard-coded FQNs scattered around (must be in `SpiralFrameworkClasses`).
- `println` / `System.out` — replace with `Logger.getInstance(...)`.
- `!!` on platform-returned nullables — risky; use `?:` or graceful fallback.
- Companion-object constants should be `const val` when possible.
- Unused imports / unused parameters / dead code.
- Public visibility on classes that should be `internal`.
- `var` instead of `val` where mutation isn't needed.

## Output format expected from each subagent

Each subagent writes ONE markdown file at the absolute path it is given, with this structure:

```
# Analysis: <relative source path>

## Summary
1-3 sentences describing the file's responsibility.

## Issues
For each issue: a numbered heading with severity tag.

### 1. [SEVERITY] Short title
- Location: <relative path>:<line range>
- Problem: <what's wrong>
- Why it matters: <impact>
- Suggested fix: <concrete remediation>

Severity tags: [BUG], [PERF], [API-MISUSE], [STYLE], [MAINTAINABILITY], [I18N], [THREAD], [DUMB-MODE], [INDEX], [LEAK].

## No-issue note
If nothing meaningful is found, write "No significant issues found." and stop.
```

Keep findings concrete, with file:line references. Don't speculate about issues that aren't present.
