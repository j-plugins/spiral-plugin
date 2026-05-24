<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# spiral-plugin Changelog

## [Unreleased]

### Added
- Test suite covering indexes, references, line markers, folding, and run-configuration settings (30 new test classes; full `gradle test` is now meaningful).
- `com.intellij.modules.json` added to `platformBundledPlugins` so the PHP plugin loads in the test sandbox (was the root cause of an empty effective extension set).
- `VIEWS_RENDER_SIGNATURE` and `ENV_FUNCTION` / `AUTOWIRE` constants in `SpiralFrameworkClasses` — replaces hardcoded FQNs scattered across `ViewReferenceContributor`, `EnvFoldingBuilder`, and `ContainerReferenceContributor`.

### Fixed
- **Run-configuration settings corruption** — `SpiralConsoleCommandRunConfigurationSettings` aliased `workingDirectory` and `documentRoot` onto the `binary` XML attribute. Each property now persists under its own slot; existing saved configurations migrate cleanly.
- **`ObjectStreamDataExternalizer.read` NPE** on null payloads — cast widened from `as T` to `as T?` so callers can decide how to handle null.
- **CQRS implicit usage over-reporting** — `CqrsHandlersImplicitUsageProvider.isClassWithCustomizedInitialization` returned `true` unconditionally, marking every PHP class as having customized init.
- **CQRS gutter icons missing** — `CqrsHandlersLineMarkerProvider.getLineMarkerInfo` matched on `PhpClass`, but the platform calls line markers on leaf elements only; re-anchored to the class-name identifier.
- **Dumb-mode crashes** at three call sites that query `PrototypedIndex` / `CqrsIndexUtil` without `DumbService` guarding — `PrototypedCompletion`, `CqrsHandlersLineMarkerProvider`, and `PrototypedPropertyReference` now early-return while indexing.

### Changed
- `gradle.properties` `platformBundledPlugins` extended with the PHP plugin's transitive `com.intellij.modules.json` requirement.
- Folding placeholder for `env('KEY')` calls uses the central `SpiralFrameworkClasses.ENV_FUNCTION` constant.

