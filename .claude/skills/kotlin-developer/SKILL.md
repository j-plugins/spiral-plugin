---
name: kotlin-developer
description: Write idiomatic Kotlin code that fits this repository's stack (Kotlin on JVM 21, IntelliJ Platform). Use this skill whenever you add or modify `.kt` files in this repo - it enforces Kotlin coding conventions, nullability rules, scope function choices, and the project's specific idioms (PSI helpers, extension fn patterns, bundle/icon singletons).
---

# Kotlin Developer Skill

Use this skill when writing or refactoring Kotlin code in this IntelliJ plugin repository.

## Stack Baseline (non-negotiable)

- **Kotlin** — JVM toolchain **21** (`kotlin { jvmToolchain(21) }` in `build.gradle.kts`).
- `kotlin.stdlib.default.dependency = false` — the IDE provides the Kotlin stdlib at runtime. **Never add `org.jetbrains.kotlin:kotlin-stdlib` as an explicit Gradle dep.**
- **No KotlinX Coroutines runtime** is declared — if you need coroutines, use the IntelliJ Platform's bundled coroutines (`com.intellij.openapi.application.*`), not raw `kotlinx.coroutines`.
- JUnit 4 only in tests — no JUnit 5 / Jupiter.

## Package & File Layout

- Base package: `com.github.xepozz.spiral.*`.
- Organize by **feature** (Spiral subsystem), not by layer: `router/`, `cqrs/`, `views/`, `console/`, `config/`, `prototyped/`, `container/`, `forms/`, `scaffolder/`.
- Within a feature package, use sub-packages:
  - `index/` — `FileBasedIndexExtension` implementations + `*IndexUtil` singleton.
  - `references/` — `PsiReferenceContributor`, `PsiReferenceProvider`, individual `PsiReference` implementations.
  - Keep top-level providers (`*LineMarkerProvider`, `*ImplicitUsageProvider`, `*CompletionContributor`) directly under the feature package.
- **One top-level class per file** for classes; small related extension functions can share a file (see `php/mixin.kt`).

## Naming

| Kind | Convention | Example from this repo |
|---|---|---|
| Package | lowercase, no `_` | `com.github.xepozz.spiral.cqrs.index` |
| Class / Object | UpperCamelCase | `SpiralConsoleCommandRunConfiguration` |
| Function | lowerCamelCase | `getConsoleCommandName()` |
| Property / local | lowerCamelCase | `phpIndex`, `nameIdentifier` |
| `const val` | SCREAMING_SNAKE_CASE | `AS_COMMAND`, `PROTOTYPE_TRAIT` |
| Acronyms | 2 letters up, 3+ only first cap | `CqrsIndexUtil` (not `CQRSIndexUtil`), `PHPLanguageInjector` is accepted here because PHP is the language identifier |

## Formatting

- 4-space indent, no tabs. Opening brace on same line, closing on its own line.
- Prefer **expression body** for single-expression functions:
  ```kotlin
  override fun getName() = "Spiral"
  override fun getDescription() = "Spiral Framework project template"
  ```
- Long expression body: put `=` on the first line, expression continues on the next indented line.
- Spaces: around binary operators; **no** space around `.`, `?.`, `::`, range `..`, unary `++/--`, generic `<>`.
- Space **before** `:` when separating subtype, **no** space before `:` when separating type from name: `class Foo : Bar`, `val x: Int`.
- **Trailing commas are encouraged** for multi-line declarations/calls — cleaner diffs when reordering:
  ```kotlin
  class SpiralEndpoint(
      val url: String,
      val method: String,
      val fqn: String,
      val group: String,
  )
  ```

## Nullability & Immutability

- **Prefer `val`** — use `var` only when mutation is essential.
- **Prefer immutable collection types** (`List`, `Set`, `Map`) at API boundaries. Only surface `MutableList` when the caller must mutate.
- Use **safe calls `?.`** and **Elvis `?:`** instead of explicit null checks where it reads cleanly.
- Use the early-return pattern with `?: return` / `?: return null` for null guards (matches existing code style):
  ```kotlin
  val element = parameters.position.parent as? FieldReference ?: return
  val phpClass = PsiTreeUtil.getParentOfType(element, PhpClass::class.java) ?: return
  if (!phpClass.hasTrait(SpiralFrameworkClasses.PROTOTYPE_TRAIT)) return
  ```
- **Do not use `!!`** unless you can justify it in a comment. Prefer smart casts, `?:`, or `requireNotNull(...)`.
- **Smart-cast with `as?`** rather than `instanceof`-then-cast:
  ```kotlin
  val fieldRef = element as? FieldReference ?: return
  ```

## Scope Functions — when to use which

| Function | Receiver | Returns | Use when |
|---|---|---|---|
| `let` | `it` (lambda param) | lambda result | Transform / null-check with `?.let { }` |
| `run` | `this` | lambda result | Compute a value using the receiver's API |
| `with` | `this` | lambda result | Group calls on an object (non-null only) |
| `apply` | `this` | receiver | Builder-style configuration |
| `also` | `it` | receiver | Side effects (logging, debugging) without breaking a chain |

Patterns already used in this repo:
```kotlin
// Chaining transformations with .let - preferred over nested lets
.value
?.run { StringUtil.unquoteString(this) }

// apply for side effects that return the receiver
.map { ... }
.apply { result.addAllElements(this) }
```

## Control Flow Idioms

- Use **expression `if`/`when`** (return the value), not statement form:
  ```kotlin
  return if (isQuery) queryHandlers() else commandHandlers()
  ```
- Use `when` for **3+** branches or type-dispatch; use `if` for **binary** conditions.
- Use `when` for smart-cast dispatch:
  ```kotlin
  return when (filter) {
      is ModuleEndpointsFilter -> ...
      else -> emptyList()
  }
  ```
- Prefer **collection operations** (`map`, `filter`, `flatMap`, `groupBy`, `sortedBy`) over manual loops. Pattern used throughout:
  ```kotlin
  RouterIndexUtil.getAllRoutes(project)
      .flatMap { route -> route.methods.map { SpiralEndpoint(...) } }
      .sortedBy { it.url }
      .groupBy { it.group }
      .map { (group, routes) -> SpiralGroup(project, group, routes) }
  ```
- Use `..<` for open-ended ranges (not `0..n - 1`).

## Extension Functions — repo convention

This repo centralizes reusable PSI extensions in `php/mixin.kt`. When you find yourself writing the same PSI traversal twice, **promote it to an extension fn there**:

```kotlin
// Already in php/mixin.kt
fun PhpClass.hasTrait(fqn: String): Boolean = traits.any { it.fqn == fqn }
fun PhpClass.hasInterface(fqn: String): Boolean = implementedInterfaces.any { it.fqn == fqn }
fun PhpClass.hasSuperClass(fqn: String): Boolean = superClasses.any { it.fqn == fqn }
```

Rules for adding extension fns:
- Keep them **pure** (no side effects, no project service lookups inside).
- If it does a project/IDE lookup, make it a utility object method (e.g. `RouterIndexUtil`), not an extension fn.
- Don't extend types you don't own with operator overloads — stick to named `fun`.

## Central Constants

- **All Spiral framework FQNs** go in `com.github.xepozz.spiral.SpiralFrameworkClasses` as `const val` with the **leading backslash** (`\Spiral\...`). Never hardcode an FQN inline.
- **View / path constants** go in `SpiralViewUtil`.
- **Icons** are accessed via `SpiralIcons` (`SpiralIcons.SPIRAL`). Load new icons there, not inline.
- **User-facing strings** go through `SpiralBundle.message("key", *args)` with keys defined in `src/main/resources/messages/SpiralBundle.properties`.

## Objects vs Companion Objects

- Use a **top-level `object`** for pure utilities with no instance-per-registration semantics (see `SpiralFrameworkClasses`, `SpiralIcons`, `SpiralViewUtil`, `RouterIndexUtil`, `CqrsIndexUtil`).
- Use a **`companion object`** only when the enclosing class needs type-level state/factories. Example in this repo:
  ```kotlin
  companion object {
      const val ID = "SpiralConsoleCommandRunConfiguration"
      val INSTANCE = SpiralConsoleCommandRunConfigurationType()
  }
  ```

## What to Avoid

- `println(...)` for logging — there are a few legacy ones; don't propagate. Use `com.intellij.openapi.diagnostic.Logger.getInstance(javaClass)`.
- Global mutable singletons with lateinit vars.
- Catching `Throwable` / broad `Exception` silently. If needed, use `com.intellij.openapi.diagnostic.Logger.error(...)` and rethrow `ProcessCanceledException` / `ControlFlowException`.
- `TODO()` and `NotImplementedError` left in committed code — replace or open an issue.
- Explicit `return Unit`, `: Unit` on functions, and explicit `public` modifiers.
- Introducing helpers / abstractions for a single call-site. Three repetitions is the threshold for extraction.

## Workflow when editing existing code

1. **Read the file first** (`Read` tool) — never edit without seeing the surrounding style.
2. Check whether a helper already exists in `php/mixin.kt`, `common/`, `SpiralFrameworkClasses`, `SpiralIcons`, or the feature's `*IndexUtil` before writing a new one.
3. Match existing formatting (indent, wrapping, trailing commas) exactly — don't reformat unrelated code.
4. Build locally before claiming success: `./gradlew compileKotlin` is the fastest signal; `./gradlew check` runs tests + Kover.
