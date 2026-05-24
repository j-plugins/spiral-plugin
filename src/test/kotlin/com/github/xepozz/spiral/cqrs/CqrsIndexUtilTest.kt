package com.github.xepozz.spiral.cqrs

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.php.lang.PhpFileType

class CqrsIndexUtilTest : BasePlatformTestCase() {

    fun testFindCommandHandlersReturnsHandlerMethodForKnownCommand() {
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
            class RegisterCommand {}
            """.trimIndent()
        )
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
            <?php
            namespace App\Handler;
            use Spiral\Cqrs\Attribute\CommandHandler;
            use App\Command\RegisterCommand;

            class RegisterHandler {
                #[CommandHandler]
                public function __invoke(RegisterCommand ${'$'}command): void {}
            }
            """.trimIndent()
        )

        val handlers = CqrsIndexUtil.findCommandHandlers("\\App\\Command\\RegisterCommand", project)
        assertTrue(
            "findCommandHandlers should return handler method FQN, got: $handlers",
            handlers.any { it.contains("RegisterHandler") && it.endsWith(".__invoke") }
        )
    }

    fun testFindQueryHandlersReturnsHandlerMethodForKnownQuery() {
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
            <?php
            namespace Spiral\Cqrs\Attribute;
            #[\Attribute] class QueryHandler {}
            """.trimIndent()
        )
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
            <?php
            namespace App\Query;
            class FetchProfileQuery {}
            """.trimIndent()
        )
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
            <?php
            namespace App\Handler;
            use Spiral\Cqrs\Attribute\QueryHandler;
            use App\Query\FetchProfileQuery;

            class FetchProfileHandler {
                #[QueryHandler]
                public function __invoke(FetchProfileQuery ${'$'}query): array { return []; }
            }
            """.trimIndent()
        )

        val handlers = CqrsIndexUtil.findQueryHandlers("\\App\\Query\\FetchProfileQuery", project)
        assertTrue(
            "findQueryHandlers should return handler method FQN, got: $handlers",
            handlers.any { it.contains("FetchProfileHandler") && it.endsWith(".__invoke") }
        )
    }

    fun testFindCommandHandlersReturnsEmptyForUnknownCommand() {
        val handlers = CqrsIndexUtil.findCommandHandlers("\\Nonexistent\\Command", project)
        assertTrue("Unknown command FQN should yield no handlers, got: $handlers", handlers.isEmpty())
    }
}
