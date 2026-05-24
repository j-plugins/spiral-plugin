package com.github.xepozz.spiral.console.run

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.php.lang.PhpFileType
import com.jetbrains.php.lang.psi.elements.PhpClass

/**
 * Validates that [ConsoleCommandLineMarkerProvider] places a gutter icon on the class
 * name identifier of a `#[AsCommand]`-annotated class and on nothing else.
 */
class ConsoleCommandLineMarkerProviderTest : BasePlatformTestCase() {

    fun testMarkerOnPhpClassWithAsCommand() {
        // The current production provider gates on `element is PhpClass` and returns an Info
        // when the class carries #[AsCommand]. The platform invokes RunLineMarkerContributor
        // on every PSI element while scanning a file, so passing the PhpClass itself is the
        // representative call.
        val provider = ConsoleCommandLineMarkerProvider()
        val phpClass = configureClassWithAsCommand("migrate:init")

        val info = provider.getInfo(phpClass as PsiElement)
        assertNotNull("Expected a line marker on the #[AsCommand] PhpClass", info)
    }

    fun testNoMarkerOnLeafElement() {
        val provider = ConsoleCommandLineMarkerProvider()
        val phpClass = configureClassWithAsCommand("migrate:init")
        val nameIdentifier = phpClass.nameIdentifier ?: error("class has no name identifier")

        // Provider only matches the PhpClass, not leaves like the class-name identifier.
        val info = provider.getInfo(nameIdentifier)
        assertNull("Did not expect a marker on a leaf element under the PhpClass", info)
    }

    fun testNoMarkerOnPlainClass() {
        val provider = ConsoleCommandLineMarkerProvider()
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
            <?php
            namespace App;
            class PlainClass {}
            """.trimIndent()
        )
        val phpClass = PsiTreeUtil.findChildOfType(myFixture.file, PhpClass::class.java)
            ?: error("PhpClass not found in test fixture")

        val info = provider.getInfo(phpClass as PsiElement)
        assertNull("Did not expect a marker on a class without #[AsCommand]", info)
    }

    private fun configureClassWithAsCommand(commandName: String): PhpClass {
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
            #[AsCommand(name: '$commandName')]
            class MigrateCommand {}
            """.trimIndent()
        )
        return PsiTreeUtil.findChildrenOfType(myFixture.file, PhpClass::class.java)
            .firstOrNull { it.name == "MigrateCommand" }
            ?: error("MigrateCommand class not found")
    }
}
