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

    fun testUntypedHandlerParameterIndexesEmptyValue() {
        // Current production contract: when a #[CommandHandler] method's first parameter has no
        // class type hint, the indexer still emits the method FQN as a key, with an empty-string
        // value (the unresolved command FQN). Bumping index version to skip these would require a
        // `getVersion()` bump and is out of scope here.
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
        val untypedKey = keys.firstOrNull { it.endsWith(".__invoke") && it.contains("UntypedHandler") }
        assertNotNull("UntypedHandler.__invoke should still be indexed, got keys: $keys", untypedKey)
        val values = FileBasedIndex.getInstance().getValues(CqrsCommandHandlerIndex.key, untypedKey!!, scope)
        assertTrue(
            "Untyped handler value is the empty string per current production indexer; got: $values",
            values.contains("")
        )
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
