package com.github.xepozz.spiral.cqrs.index

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.indexing.FileBasedIndex
import com.jetbrains.php.lang.PhpFileType

class CqrsCommandIndexTest : BasePlatformTestCase() {

    fun testCommandFqnIsIndexedAsKeyToHandlerMethod() {
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
            <?php
            namespace Spiral\Cqrs\Attribute;
            #[\Attribute] class CommandHandler {}
            """.trimIndent()
        )
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
            <?php
            namespace App\Command;
            class CreateUserCommand {}
            """.trimIndent()
        )
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

        val scope = GlobalSearchScope.allScope(project)
        val handlers = FileBasedIndex.getInstance()
            .getValues(CqrsCommandIndex.key, "\\App\\Command\\CreateUserCommand", scope)
        assertTrue(
            "Index should map command FQN -> handler method FQN, got: $handlers",
            handlers.any { it.contains("CreateUserHandler") && it.endsWith(".__invoke") }
        )
    }

    fun testHandlerWithoutCommandTypeProducesNoIndexEntry() {
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
            <?php
            namespace Spiral\Cqrs\Attribute;
            #[\Attribute] class CommandHandler {}
            """.trimIndent()
        )
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
            <?php
            namespace App\Handler;
            use Spiral\Cqrs\Attribute\CommandHandler;

            class UntypedHandler {
                #[CommandHandler]
                public function __invoke(${'$'}untyped): void {}
            }
            """.trimIndent()
        )

        val keys = FileBasedIndex.getInstance().getAllKeys(CqrsCommandIndex.key, project)
        assertFalse(
            "Empty-string key must never be indexed, got: $keys",
            keys.any { it.isEmpty() }
        )
    }
}
