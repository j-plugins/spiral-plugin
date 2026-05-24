package com.github.xepozz.spiral.cqrs

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.php.lang.PhpFileType

class CqrsHandlersLineMarkerProviderTest : BasePlatformTestCase() {

    fun testGutterAppearsOnCommandClass() {
        // Stubs in dedicated files so PhpIndex sees them on lookup.
        myFixture.addFileToProject(
            "stubs/CommandInterface.php",
            """
            <?php
            namespace Spiral\Cqrs;
            interface CommandInterface {}
            """.trimIndent()
        )
        myFixture.addFileToProject(
            "stubs/CommandHandler.php",
            """
            <?php
            namespace Spiral\Cqrs\Attribute;
            #[\Attribute] class CommandHandler {}
            """.trimIndent()
        )
        myFixture.addFileToProject(
            "src/Handler/CreateUserHandler.php",
            """
            <?php
            namespace App\Handler;
            use Spiral\Cqrs\Attribute\CommandHandler;
            use App\Command\CreateUserCommand;

            class CreateUserHandler {
                #[CommandHandler]
                public function __invoke(CreateUserCommand ${'$'}command): void {}
            }
            """.trimIndent()
        )
        // The command class — caret is positioned on its identifier
        myFixture.configureByText(
            "CreateUserCommand.php",
            """
            <?php
            namespace App\Command;
            use Spiral\Cqrs\CommandInterface;

            class CreateUs<caret>erCommand implements CommandInterface {}
            """.trimIndent()
        )

        myFixture.doHighlighting()
        val allGutters = myFixture.findAllGutters()
        val gutterTexts = allGutters.map { it.tooltipText }
        assertTrue(
            "Expected at least one 'Navigate to handler' gutter on the file; tooltips=$gutterTexts",
            gutterTexts.any { it?.contains("Navigate to handler") == true }
        )
    }

    fun testNoGutterOnUnrelatedClass() {
        myFixture.configureByText(
            "PlainClass.php",
            """
            <?php
            namespace App;
            class Plain<caret>Class {}
            """.trimIndent()
        )
        myFixture.doHighlighting()
        val allGutters = myFixture.findAllGutters()
        val gutterTexts = allGutters.map { it.tooltipText }
        assertTrue(
            "Plain class should not get a CQRS handler gutter, got: $gutterTexts",
            gutterTexts.none { it?.contains("Navigate to handler") == true }
        )
    }
}
