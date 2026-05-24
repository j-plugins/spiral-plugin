package com.github.xepozz.spiral.config.env

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class EnvFoldingBuilderTest : BasePlatformTestCase() {

    fun testEnvFunctionCallIsFolded() {
        myFixture.configureByText(
            "env_usage.php",
            """
            <?php
            function env(string ${'$'}key, mixed ${'$'}default = null) {}

            ${'$'}value = env('APP_DEBUG');
            """.trimIndent()
        )
        val foldRegions = myFixture.editor.foldingModel.allFoldRegions
        assertTrue(
            "Expected at least one fold region for env() call",
            foldRegions.any { it.placeholderText == "env: 'APP_DEBUG'" }
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
        val foldRegions = myFixture.editor.foldingModel.allFoldRegions
        assertTrue(
            "env() with no arguments must not be folded",
            foldRegions.none { it.placeholderText.startsWith("env") }
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
        val foldRegions = myFixture.editor.foldingModel.allFoldRegions
        assertTrue(
            "Unrelated function call must not be folded",
            foldRegions.none { it.placeholderText.startsWith("env") }
        )
    }
}
