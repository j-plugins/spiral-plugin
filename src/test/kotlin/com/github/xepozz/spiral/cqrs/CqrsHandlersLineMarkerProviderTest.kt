package com.github.xepozz.spiral.cqrs

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.php.lang.PhpFileType

class CqrsHandlersLineMarkerProviderTest : BasePlatformTestCase() {

    fun testGutterAppearsOnCommandClass() {
        // CommandInterface stub
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
            <?php
            namespace Spiral\Cqrs;
            interface CommandInterface {}
            """.trimIndent()
        )
        // CommandHandler attribute stub
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
            <?php
            namespace Spiral\Cqrs\Attribute;
            #[\Attribute] class CommandHandler {}
            """.trimIndent()
        )
        // Handler indexed by file-based index
        myFixture.configureByText(
            PhpFileType.INSTANCE,
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

        val gutters = myFixture.findGuttersAtCaret()
        assertTrue(
            "Expected at least one gutter on the command class identifier; tooltips=${gutters.map { it.tooltipText }}",
            gutters.any { it.tooltipText?.contains("Navigate to handler") == true }
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
        val gutters = myFixture.findGuttersAtCaret()
        assertTrue(
            "Plain class should not get a CQRS handler gutter, got: ${gutters.map { it.tooltipText }}",
            gutters.none { it.tooltipText?.contains("Navigate to handler") == true }
        )
    }
}
