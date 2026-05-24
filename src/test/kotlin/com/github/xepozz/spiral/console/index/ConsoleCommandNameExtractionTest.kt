package com.github.xepozz.spiral.console.index

import com.github.xepozz.spiral.php.getConsoleCommandName
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.php.lang.PhpFileType
import com.jetbrains.php.lang.psi.elements.PhpClass

/**
 * Verifies the `#[AsCommand(name: ...)]` extraction helper used by the console
 * commands index, the line marker, and the run configuration producer.
 */
class ConsoleCommandNameExtractionTest : BasePlatformTestCase() {

    fun testNamedArgument() {
        val phpClass = configure(
            """
            #[AsCommand(name: 'migrate:init')]
            class MigrateCommand {}
            """.trimIndent()
        )
        assertEquals("migrate:init", phpClass.getConsoleCommandName())
    }

    fun testPositionalArgument() {
        val phpClass = configure(
            """
            #[AsCommand('cache:clear')]
            class CacheClearCommand {}
            """.trimIndent()
        )
        assertEquals("cache:clear", phpClass.getConsoleCommandName())
    }

    fun testNoAttributeReturnsNull() {
        val phpClass = configure(
            """
            class PlainCommand {}
            """.trimIndent()
        )
        assertNull(phpClass.getConsoleCommandName())
    }

    private fun configure(body: String): PhpClass {
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
            <?php
            namespace Spiral\Console\Attribute;
            #[\Attribute(\Attribute::TARGET_CLASS)]
            class AsCommand {
                public function __construct(public string ${'$'}name = '') {}
            }
            namespace App;
            use Spiral\Console\Attribute\AsCommand;
            $body
            """.trimIndent()
        )
        return PsiTreeUtil.findChildrenOfType(myFixture.file, PhpClass::class.java)
            .firstOrNull { it.name != "AsCommand" }
            ?: error("Target class not found in test fixture")
    }
}
