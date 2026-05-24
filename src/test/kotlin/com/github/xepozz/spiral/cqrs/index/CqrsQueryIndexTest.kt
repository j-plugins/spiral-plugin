package com.github.xepozz.spiral.cqrs.index

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.indexing.FileBasedIndex
import com.jetbrains.php.lang.PhpFileType

class CqrsQueryIndexTest : BasePlatformTestCase() {

    fun testQueryFqnIsIndexedAsKeyToHandlerMethod() {
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
            class FindUserQuery {}
            """.trimIndent()
        )
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
            <?php
            namespace App\Handler;
            use Spiral\Cqrs\Attribute\QueryHandler;
            use App\Query\FindUserQuery;

            class FindUserHandler {
                #[QueryHandler]
                public function __invoke(FindUserQuery ${'$'}query): array { return []; }
            }
            """.trimIndent()
        )

        val scope = GlobalSearchScope.allScope(project)
        val handlers = FileBasedIndex.getInstance()
            .getValues(CqrsQueryIndex.key, "\\App\\Query\\FindUserQuery", scope)
        assertTrue(
            "Index should map query FQN -> handler method FQN, got: $handlers",
            handlers.any { it.contains("FindUserHandler") && it.endsWith(".__invoke") }
        )
    }

    fun testHandlerWithoutQueryTypeProducesNoIndexEntry() {
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
            namespace App\Handler;
            use Spiral\Cqrs\Attribute\QueryHandler;

            class UntypedQueryHandler {
                #[QueryHandler]
                public function __invoke(${'$'}untyped): array { return []; }
            }
            """.trimIndent()
        )

        val keys = FileBasedIndex.getInstance().getAllKeys(CqrsQueryIndex.key, project)
        assertFalse(
            "Empty-string key must never be indexed, got: $keys",
            keys.any { it.isEmpty() }
        )
    }
}
