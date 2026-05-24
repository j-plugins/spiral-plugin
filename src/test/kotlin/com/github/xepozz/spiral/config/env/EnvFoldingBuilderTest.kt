package com.github.xepozz.spiral.config.env

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class EnvFoldingBuilderTest : BasePlatformTestCase() {

    private fun buildFoldRegions(): List<com.intellij.lang.folding.FoldingDescriptor> {
        val builder = EnvFoldingBuilder()
        val descriptors = builder.buildFoldRegions(myFixture.file, myFixture.editor.document, false)
        return descriptors.filterNotNull()
    }

    fun testEnvFunctionCallIsFolded() {
        myFixture.configureByText(
            "env_usage.php",
            """
            <?php
            function env(string ${'$'}key, mixed ${'$'}default = null) {}

            ${'$'}value = env('APP_DEBUG');
            """.trimIndent()
        )
        val descriptors = buildFoldRegions()
        assertTrue(
            "Expected at least one fold region for env() call; got: ${descriptors.map { it.placeholderText }}",
            descriptors.any { it.placeholderText == "env: 'APP_DEBUG'" }
        )
    }

    fun testEnvFunctionWithoutArgumentsIsNotFolded() {
        myFixture.configureByText(
            "env_empty.php",
            """
            <?php
            function env(string ${'$'}key = '', mixed ${'$'}default = null) {}

            ${'$'}value = env();
            """.trimIndent()
        )
        val descriptors = buildFoldRegions()
        assertTrue(
            "env() with no arguments must not be folded; got: ${descriptors.map { it.placeholderText }}",
            descriptors.none { (it.placeholderText ?: "").startsWith("env") }
        )
    }

    fun testUnrelatedFunctionIsNotFolded() {
        myFixture.configureByText(
            "other.php",
            """
            <?php
            function foo(string ${'$'}key) {}

            ${'$'}value = foo('APP_DEBUG');
            """.trimIndent()
        )
        val descriptors = buildFoldRegions()
        assertTrue(
            "Unrelated function call must not be folded; got: ${descriptors.map { it.placeholderText }}",
            descriptors.none { (it.placeholderText ?: "").startsWith("env") }
        )
    }
}
