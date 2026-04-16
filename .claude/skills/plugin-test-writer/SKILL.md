---
name: plugin-test-writer
description: Write IntelliJ Platform tests for this Kotlin plugin (JUnit 4 + BasePlatformTestCase + CodeInsightTestFixture). Use this skill whenever you add or modify tests under `src/test/`. It covers fixture patterns for references, completion, inspections, line markers, rename, folding, file-based indexes, and the testData directory conventions.
---

# Plugin Test Writer Skill

Use this skill when writing or updating tests for the Spiral Framework plugin.

## Test Stack

- **Framework:** JUnit **4** (tests use `fun testXxx()` naming convention, not `@Test` annotations on JUnit 5 style).
- **Base class:** `com.intellij.testFramework.fixtures.BasePlatformTestCase` — preferred for almost all tests. Backed by an in-memory VirtualFileSystem and a light project.
- **Fixture:** `myFixture` property of type `CodeInsightTestFixture` (inherited from `BasePlatformTestCase`).
- **Assertions:** inherited `assertEquals`, `assertNotNull`, `assertInstanceOf`, `assertFalse`, `assertNotSame`, `fail(...)` from `UsefulTestCase`. Do **not** introduce AssertJ / Kotest / Hamcrest.
- **Dependencies:** already in `libs.versions.toml` — `junit 4.13.2`, `opentest4j 1.3.0`, plus `testFramework(TestFrameworkType.Platform)` from the IntelliJ Platform Gradle Plugin.
- **Coverage:** Kotlinx Kover runs automatically on `./gradlew check` and writes XML to `build/reports/kover/report.xml`.

Run locally:
- **All tests + coverage:** `./gradlew check` (or use the `Run Tests` run configuration in `.run/`).
- **Specific test class:** `./gradlew test --tests "com.github.xepozz.spiral.router.RouterReferenceTest"`.

## Philosophy

IntelliJ Platform tests are **model-level functional tests**, not isolated unit tests:
- They run against **real** PSI, VFS, and indexing infrastructure in a headless IDE process.
- Mocking is **discouraged** — use the real platform components.
- Tests are stable across refactors because they exercise behavior, not implementation.
- Prefer **"configure input file → invoke action → check result"** over asserting internal state.

## Directory Layout

```
src/test/
├── kotlin/com/github/xepozz/spiral/
│   └── <feature>/...Test.kt       # mirror src/main structure
└── testData/                       # input + expected fixture files
    ├── rename/
    │   ├── foo.xml                 # before
    │   └── foo_after.xml           # after
    ├── completion/
    │   └── some_case.php
    ├── references/
    │   └── ...
    └── index/
        └── ...
```

- Override `getTestDataPath()` to point at the subdirectory your test reads from:
  ```kotlin
  override fun getTestDataPath() = "src/test/testData/rename"
  ```
- Annotate the class with `@TestDataPath("\$CONTENT_ROOT/src/test/testData")` so the IDE can navigate from the test to the fixture (helps PsiViewer too).
- Keep input/expected pairs adjacent. Conventional suffixes: `_after.ext`, `.after.ext`, or pair files in named sub-folders.

## Skeleton — minimal test file

```kotlin
package com.github.xepozz.spiral.router

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase

@TestDataPath("\$CONTENT_ROOT/src/test/testData")
class RouterReferenceTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData/router"

    fun testResolvesRouteNameReference() {
        myFixture.configureByFile("route_name_reference.php")
        val ref = myFixture.file.findReferenceAt(myFixture.caretOffset) ?: fail("no reference")
        val resolved = ref.resolve()
        assertNotNull("reference should resolve", resolved)
    }
}
```

**Key conventions:**
- Test class names end in `Test` and mirror the subject's package.
- Test methods start with `test` (JUnit 4 discovery).
- Use `myFixture.caretOffset` where a `<caret>` marker exists in the fixture file.

## Common Fixture Methods

| Method | Purpose |
|---|---|
| `configureByText(fileType, "content")` | Load an in-memory file; good for tiny cases. |
| `configureByFile("relative.ext")` | Load from `getTestDataPath()`. Use `<caret>` in the fixture to set caret. |
| `configureByFiles(a, b, c)` | Multiple files; last one is the "active" editor file. |
| `complete()` / `completeBasic()` | Trigger completion. Returns `Array<LookupElement>?`; `null` if single item was inserted. |
| `completeBasicAllCarets(null)` | Completion at every `<caret>` marker. |
| `lookupElementStrings` | Shortcut for the current completion list (`List<String>?`). |
| `checkResult("expected.ext")` or `checkResultByFile(...)` | Assert the editor matches an expected file. |
| `checkResultByFile("before", "after", true)` | Compare with stripped trailing spaces. |
| `doHighlighting()` | Run inspections + annotators; returns `List<HighlightInfo>`. |
| `findGuttersAtCaret()` / `findAllGutters()` | Assert line marker / gutter icons. |
| `testRename("before.ext", "after.ext", "newName")` | End-to-end rename refactor. |
| `testFolding("file.ext")` | Assert folding using `<fold text='...'>...</fold>` markers. |
| `copyDirectoryToProject(src, dest)` | Materialize a directory inside the light project (used for multi-file tests). |
| `addFileToProject("rel/path", "content")` | Add an extra file without opening it. |

## Patterns by Feature Area

### 1. PsiReferenceContributor tests

```kotlin
fun testRouteNameResolves() {
    myFixture.configureByText(
        PhpFileType.INSTANCE,
        """<?php
        #[\Spiral\Router\Annotation\Route(route: '/foo', name: 'foo.bar')]
        class Ctrl {
            public function action() {
                \$url = route('foo.<caret>bar');
            }
        }
        """.trimIndent()
    )
    val ref = myFixture.file.findReferenceAt(myFixture.caretOffset)
    assertNotNull(ref)
    assertNotNull(ref!!.resolve())
}
```

- Use `findReferenceAt(myFixture.caretOffset)` — the caret is placed by the `<caret>` token in the source.
- For multi-resolve references, cast to `PsiPolyVariantReference` and assert on `multiResolve(false).size`.
- For completion *variants* produced by a reference's `getVariants()`, use `myFixture.completeBasic()` and inspect `lookupElementStrings`.

### 2. Completion contributor tests

```kotlin
fun testPrototypedCompletionSuggestsBoundProperties() {
    myFixture.configureByFile("prototyped_completion.php")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "logger", "cache")
}
```

- `assertContainsElements(actual, expected...)` is inherited from `UsefulTestCase` — prefer it over manual `contains` loops.
- To assert an **exact** set, use `assertSameElements`.
- To assert the completion list is ordered, call `myFixture.completeBasic()` and iterate the returned `LookupElement[]`.

### 3. File-based index tests

```kotlin
fun testRouterNamesIndexContainsRoute() {
    myFixture.configureByFile("routes_fixture.php")
    val keys = FileBasedIndex.getInstance()
        .getAllKeys(RouterNamesIndex.KEY, project)
    assertTrue("foo.bar" in keys)
}
```

- Configure the file first so that indexing runs against it.
- Always query through `FileBasedIndex.getInstance()` with the concrete `ID<K, V>`. Don't call the internal `indexer` directly.
- If your index registers only certain file types, the fixture file must match (e.g. `.php`).

### 4. Line marker provider tests

```kotlin
fun testCqrsHandlerGutterNavigatesToCommand() {
    myFixture.configureByFiles("CreateUser.php", "CreateUserHandler.php")
    val gutters = myFixture.findAllGutters()
    assertEquals(1, gutters.size)
}
```

- For `RelatedItemLineMarkerProvider`, also assert the navigation targets by casting the gutter's `lineMarkerInfo` and resolving.

### 5. Inspections / highlighting

```kotlin
fun testInspectionWarnsOnMissingBinding() {
    myFixture.enableInspections(MyInspection::class.java)
    myFixture.configureByFile("missing_binding.php")
    val highlights = myFixture.doHighlighting()
        .filter { it.severity == HighlightSeverity.WARNING }
    assertEquals(1, highlights.size)
}
```

### 6. Rename refactor

```kotlin
fun testRename() {
    myFixture.testRename("foo.xml", "foo_after.xml", "a2")
}
```

The existing stub `MyPluginTest.testRename` works against `src/test/testData/rename/{foo.xml,foo_after.xml}`.

### 7. Folding builder

Use `<fold text='...'>`/`</fold>` markers in the fixture to assert folding regions:
```kotlin
fun testEnvCallFolding() {
    myFixture.testFolding("${testDataPath}/env_fold.php")
}
```

### 8. Run configuration producer

For `SpiralRunConfigurationProducer`, set up a PSI context with `<caret>` inside a console command method and assert that `ConfigurationContext.createConfigurationsFromContext()` returns one matching configuration. Keep these tests small — they are slow.

## Dealing with `dumb mode` / indexing

- `BasePlatformTestCase` runs in **smart mode** by default. If you need to test dumb-mode behavior, wrap the call in `DumbServiceImpl.getInstance(project).runInDumbMode { ... }`.
- Avoid sleeps and polling. If something races, use `PsiDocumentManager.getInstance(project).commitAllDocuments()` or `IndexingTestUtil.waitUntilIndexesAreReady(project)`.

## Fixing the Template Leftover

`src/test/kotlin/com/github/xepozz/spiral/MyPluginTest.kt` still references
`com.github.xepozz.spiral.services.MyProjectService`, which does **not** exist
in `src/main`. The class does not compile. When touching it:

1. Delete the `testProjectService` method and the `import` of `MyProjectService`, **or**
2. Replace it with real tests for actual plugin features (router index, cqrs line marker, etc.).

Either way, do not re-add `MyProjectService` — it was template scaffolding.

## Assertion Style

Prefer, in this order:
1. Fixture-based assertions (`checkResultByFile`, `testRename`, `testFolding`) — they self-document via the testData file.
2. `UsefulTestCase` helpers: `assertContainsElements`, `assertSameElements`, `assertOrderedEquals`, `assertSize`, `assertInstanceOf`.
3. Plain `assertEquals(expected, actual, message)` / `assertTrue(message, cond)`.
4. `fail("descriptive reason")` inside an Elvis when null is a test failure:
   ```kotlin
   val ref = myFixture.file.findReferenceAt(myFixture.caretOffset)
       ?: fail("no reference at caret")
   ```

Always include a **message** string in `assertTrue` / `assertEquals` when the condition isn't self-evident — failures surface in Kover / CI logs.

## What NOT to do

- **Don't** import `org.junit.jupiter.api.*` — this project is JUnit 4, not 5.
- **Don't** use `@Test` / `@Before` / `@After` annotations with `BasePlatformTestCase`. Use `fun testXxx()` and override `setUp()` / `tearDown()` (call `super` first in `setUp`, last in `tearDown`).
- **Don't** mock `Project`, `PsiManager`, `FileBasedIndex` etc. — use the real fixture.
- **Don't** write tests that shell out to `./gradlew` or `composer`.
- **Don't** commit IDE-generated absolute paths in testData (use relative paths).
- **Don't** leave `println(...)` in tests for inspection output — they pollute CI logs.
