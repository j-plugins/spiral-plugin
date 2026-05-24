package com.github.xepozz.spiral.cqrs.index

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.indexing.FileBasedIndex
import com.jetbrains.php.lang.PhpFileType

class CqrsCommandHandlerIndexTest : BasePlatformTestCase() {

    fun testHandlerMethodIsIndexedWithCommandFqnValue() {
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
        val keys = FileBasedIndex.getInstance().getAllKeys(CqrsCommandHandlerIndex.key, project)
        val handlerMethodFqn = keys.firstOrNull { it.endsWith(".__invoke") && it.contains("CreateUserHandler") }
        assertNotNull("Handler method FQN should be indexed, got keys: $keys", handlerMethodFqn)

        val values = FileBasedIndex.getInstance().getValues(CqrsCommandHandlerIndex.key, handlerMethodFqn!!, scope)
        assertTrue(
            "Indexed command FQN should resolve to CreateUserCommand, got: $values",
            values.any { it.endsWith("\\CreateUserCommand") }
        )
    }

    fun testEmptyValueIsNotIndexedWhenCommandTypeIsAbsent() {
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

        val scope = GlobalSearchScope.allScope(project)
        val keys = FileBasedIndex.getInstance().getAllKeys(CqrsCommandHandlerIndex.key, project)
        for (k in keys) {
            val values = FileBasedIndex.getInstance().getValues(CqrsCommandHandlerIndex.key, k, scope)
            assertFalse(
                "Empty-string value must never be indexed (key=$k, values=$values)",
                values.any { it.isEmpty() }
            )
        }
    }

    fun testMethodWithoutAttributeIsNotIndexed() {
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
            <?php
            namespace App\Service;
            class PlainService {
                public function handle(${'$'}x): void {}
            }
            """.trimIndent()
        )
        val keys = FileBasedIndex.getInstance().getAllKeys(CqrsCommandHandlerIndex.key, project)
        assertTrue(
            "Plain method without #[CommandHandler] must not be indexed, got keys: $keys",
            keys.none { it.contains("PlainService") }
        )
    }
}
