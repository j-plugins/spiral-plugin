# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with this repository.

## Project Overview

**Spiral Framework Plugin** — a JetBrains IDE plugin that adds tooling support for the
[Spiral Framework](https://spiral.dev) (a modern PHP framework). It is published on the
JetBrains Marketplace as [`28608-spiral-framework`](https://plugins.jetbrains.com/plugin/28608-spiral-framework).

- **Plugin ID:** `com.github.xepozz.spiral`
- **Group:** `com.github.xepozz.spiral`
- **Target IDE:** IntelliJ IDEA Ultimate (`platformType = IU`) with PhpStorm PHP support
- **Since build:** `251` (IDE 2025.1+)
- **Mandatory runtime dependencies:** `com.intellij.modules.platform`, `com.jetbrains.php`

The plugin is based on the
[IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template).

## Tech Stack

- **Language:** Kotlin (JVM toolchain **21**)
- **Build:** Gradle 9.2.1 (Kotlin DSL) + IntelliJ Platform Gradle Plugin **2.x**
- **Dependencies catalog:** `gradle/libs.versions.toml` (Gradle Version Catalog)
- **Testing:** JUnit 4 + `BasePlatformTestCase` (IntelliJ Platform test framework)
- **Coverage:** Kotlinx Kover (XML report emitted on `check`)
- **Static analysis:** Qodana (JVM Community profile `qodana.recommended`)
- **Changelog:** `org.jetbrains.changelog` Gradle plugin (consumes `CHANGELOG.md`)

## Common Commands

All commands are run from the repository root via the Gradle wrapper.

| Command | Purpose |
|---|---|
| `./gradlew runIde` | Launch a sandboxed IDE with the plugin installed (see `.run/Run Plugin.run.xml`) |
| `./gradlew buildPlugin` | Build the distributable `.zip` under `build/distributions/` |
| `./gradlew check` | Run tests + produce Kover coverage at `build/reports/kover/report.xml` (see `.run/Run Tests.run.xml`) |
| `./gradlew test` | Run only the test task |
| `./gradlew verifyPlugin` | Run plugin structure verification + IntelliJ Plugin Verifier (see `.run/Run Verifications.run.xml`) |
| `./gradlew patchChangelog` | Apply the next version to `CHANGELOG.md` |
| `./gradlew publishPlugin` | Publish to JetBrains Marketplace (requires `PUBLISH_TOKEN` env var) |
| `./gradlew runIdeForUiTests` | Launch IDE with `robot-server` for UI tests (port 8082) |

Signing/publishing credentials come from env vars: `CERTIFICATE_CHAIN`, `PRIVATE_KEY`, `PRIVATE_KEY_PASSWORD`, `PUBLISH_TOKEN` (see `build.gradle.kts:78-90`).

## Repository Layout

```
src/main/kotlin/com/github/xepozz/spiral/
├── SpiralFrameworkClasses.kt      # Central registry of Spiral FQNs (attributes, interfaces, traits)
├── SpiralBundle.kt                # i18n bundle accessor (messages.SpiralBundle)
├── SpiralIcons.kt                 # Plugin icon registry
├── SpiralViewUtil.kt              # View layer constants (suffix, predefined dirs)
├── SpellcheckingDictionaryProvider.kt
│
├── common/                        # Shared helpers
│   ├── index/                     # AbstractIndex, ObjectStreamDataExternalizer
│   └── references/                # AttributesUtil, InsertTextInsertHandler
├── php/                           # PHP PSI helpers (mixin.kt extension fns, patterns/)
│
├── config/                        # Config files support
│   ├── env/                       # EnvFoldingBuilder (folds env() calls)
│   └── index/                     # ConfigSectionIndex, PrototypedIndex
├── console/                       # `php app.php ...` console commands
│   ├── index/ConsoleCommandsIndex.kt
│   └── run/                       # Run configuration, line markers, run-anything provider
├── container/                     # DI container references
├── cqrs/                          # @CommandHandler / @QueryHandler navigation
│   └── index/                     # Command/Query + Handler indexes (4 indexes)
├── forms/                         # AttributesFilter implicit usage provider
├── prototyped/                    # @Prototyped / PrototypeTrait completion & typing
├── references/                    # FunctionsReferenceContributor, DirectoryReference
├── router/                        # @Route annotations → microservices endpoints
│   ├── endpoints/                 # SpiralEndpointsProvider (Endpoints tool window)
│   ├── index/                     # RouterNamesIndex, RouterUrlsIndex
│   └── references/                # URI, name, methods, group references
├── scaffolder/project/            # "New Project → Spiral" Composer-based generator
└── views/                         # *.dark.php templating
    ├── index/ViewsNamespaceIndex.kt
    ├── injection/                 # PHP → view language injector
    └── references/                # View file + namespace references

src/main/resources/
├── META-INF/plugin.xml            # Plugin manifest (extension registrations)
├── messages/SpiralBundle.properties
└── icons/spiral/icon.svg

src/test/                          # JUnit 4 + BasePlatformTestCase
```

**Important:** feature modules are organized by Spiral framework subsystem (router, cqrs, views, etc.).
When adding support for a new framework feature, mirror this layout: `<feature>/index/`,
`<feature>/references/`, plus top-level contributors/providers.

## Architecture

### Extension Wiring (`src/main/resources/META-INF/plugin.xml`)

The plugin's capabilities are exclusively wired via `plugin.xml`. When you add a new
`CompletionContributor`, `PsiReferenceContributor`, `FileBasedIndexExtension`,
`LineMarkerProvider`, `ImplicitUsageProvider`, etc., you **must** register it there.

Currently registered extension points include:
- `multiHostInjector`, `lang.foldingBuilder`
- `psi.referenceContributor` (container, functions, prototyped, views, router)
- `fileBasedIndex` × 9 (config, views, console, router × 2, cqrs × 4)
- `completion.contributor`, `spellchecker.bundledDictionaryProvider`
- `configurationType`, `runLineMarkerContributor`, `runConfigurationProducer`, `runAnything.executionProvider`
- `implicitUsageProvider` × 3, `codeInsight.lineMarkerProvider`
- `directoryProjectGenerator`, `moduleBuilder`
- `microservices.endpointsProvider`
- `com.jetbrains.php.typeProvider4` (for the prototyped type provider)

### Spiral FQN Constants

**All Spiral framework FQNs live in `SpiralFrameworkClasses.kt`.** Do not hardcode FQN
strings across feature modules — add a constant there and reference it. Current constants
cover: `InjectableConfig`, `DirectoriesInterface`, `EnvironmentInterface`, `Prototyped`,
`PrototypeTrait`, `PrototypeBootloader`, `ViewsBootloader`, `ViewsInterface`, `AsCommand`,
`Route`, `AttributesFilter`, and CQRS command/query (handler) classes.

### PHP PSI Helpers (`php/mixin.kt`)

Commonly reused extension functions on PhpStorm PSI types:
- `PhpClass.hasTrait(fqn)`, `hasInterface(fqn)`, `hasSuperClass(fqn)`
- `StringLiteralExpression.contentRange` — range inside the quotes, shifted absolute
- `PhpClass.getConsoleCommandName()` — extracts `#[AsCommand(name: ...)]`
- `PhpReference.getSignatures()` / `hasSignature(...)`

Prefer these helpers over re-implementing PSI traversal inline.

### File-Based Indexes

All indexes extend `common/index/AbstractIndex.kt` and register in `plugin.xml`.
`AbstractIndex` defaults: `dependsOnFileContent() = true`, `EnumeratorStringDescriptor`
keys, `version = 1`. **Bump `getVersion()` in the concrete index whenever you change the
indexer output shape**, otherwise stale caches will produce corrupt results.

Access patterns generally go through `*IndexUtil` singletons (e.g. `RouterIndexUtil`,
`CqrsIndexUtil`, `ViewsNamespaceIndexUtil`) rather than touching `FileBasedIndex.getInstance()`
directly from contributors.

### Run Configuration (Console Commands)

The `SpiralConsoleCommandRunConfigurationType` (id `SpiralConsoleCommandRunConfiguration`)
wires a Run Configuration for `php app.php <command>`. The full pipeline is:

1. `ConsoleCommandsIndex` indexes `#[AsCommand]` classes
2. `ConsoleCommandLineMarkerProvider` shows the gutter runner on command classes
3. `SpiralRunConfigurationProducer` / `SpiralRunAnythingProvider` create configurations
4. `SpiralConsoleCommandRunConfiguration` + `SpiralConsoleCommandSettingsEditor` run them

### Endpoints (Router)

`SpiralEndpointsProvider` feeds the **Endpoints** tool window from `RouterIndexUtil.getAllRoutes()`,
grouping by route group and presenting HTTP method + URI via `HttpMethodPresentation`.

### Project Scaffolding

`SpiralProjectGenerator` (`directoryProjectGenerator` + `moduleBuilder`) runs a
Composer-based template install (`ComposerProjectGenerator`). If `composer` isn't on
`PATH`, it falls back to a Phar executor using the first registered PHP interpreter.

## Coding Conventions

- **Kotlin target:** JVM 21 (`kotlin { jvmToolchain(21) }`). Do not downgrade.
- **Kotlin stdlib:** `kotlin.stdlib.default.dependency = false` — the plugin relies on the
  IDE-bundled stdlib. Do not add `org.jetbrains.kotlin:kotlin-stdlib` as an explicit dep.
- **No `println`-style logging in new code** (there are a few legacy ones in `SpiralEndpointsProvider`
  and `SpiralProjectGenerator` — don't propagate the pattern). Use `com.intellij.openapi.diagnostic.Logger`.
- **User-facing strings** go through `SpiralBundle.message(key, ...)` with keys in
  `src/main/resources/messages/SpiralBundle.properties`.
- **Icons** are accessed via `SpiralIcons` (single source of truth).
- **Feature module layout:** put indexes in `<feature>/index/`, references in
  `<feature>/references/`, and keep framework-agnostic contributors in the top-level
  feature package.
- **Gradle Configuration Cache and Build Cache are both enabled** (`gradle.properties`).
  Avoid introducing Gradle code that breaks configuration cache compatibility.

## CI / Release

- **`.github/workflows/build.yml`** — on every push to `main` / every PR: builds,
  runs `check`, runs Qodana inspections, runs `verifyPlugin`, uploads Kover report to
  Codecov, and creates a draft GitHub release (non-PR only).
- **`.github/workflows/release.yml`** — triggered by publishing the draft release.
- **`.github/workflows/run-ui-tests.yml`** — UI tests via `runIdeForUiTests`.
- **`CHANGELOG.md`** follows [Keep a Changelog](https://keepachangelog.com). The draft
  release notes are produced via `./gradlew getChangelog --unreleased`.

## Versioning & Platform Targets

Defined in `gradle.properties`:
- `pluginVersion` — SemVer; pre-release labels (e.g. `2025.1.0-alpha.3`) auto-select
  a Marketplace channel based on the label segment before the dot.
- `pluginSinceBuild = 251`  → minimum IDE build (2025.1).
- `platformVersion = 2025.1.1` + `platformType = IU`  → development IDE.
- `platformPlugins` lists Marketplace plugins the dev sandbox should install
  (currently PHP `com.jetbrains.php:251.23774.16` + Indices Viewer).

Bumping the IDE target requires updating **both** `platformVersion` and `pluginSinceBuild`,
and typically the matching `platformPlugins` versions too.

## Known Gotchas

- `src/test/kotlin/.../MyPluginTest.kt` is **template leftover** — it references
  `com.github.xepozz.spiral.services.MyProjectService`, which does not exist in
  `src/main`. The test will not compile as written. Prefer writing new tests against
  real plugin features; fix/replace `MyPluginTest` if you touch it.
- When adding framework FQN constants, note that Spiral FQNs in `SpiralFrameworkClasses.kt`
  are written with a **leading backslash** (`\Spiral\...`) matching PhpStorm's `PhpClass.fqn`
  convention.
- `ObjectStreamDataExternalizer` in `common/index/` uses Java serialization — index value
  classes must remain `Serializable` and backward-compatible, or `getVersion()` must be bumped.
