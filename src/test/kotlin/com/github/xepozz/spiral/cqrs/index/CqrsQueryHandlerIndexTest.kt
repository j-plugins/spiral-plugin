package com.github.xepozz.spiral.cqrs.index

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.indexing.FileBasedIndex
import com.jetbrains.php.lang.PhpFileType

class CqrsQueryHandlerIndexTest : BasePlatformTestCase() {

    fun testQueryHandlerMethodIsIndexed() {
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
        val keys = FileBasedIndex.getInstance().getAllKeys(CqrsQueryHandlerIndex.key, project)
        val handlerMethodFqn = keys.firstOrNull { it.endsWith(".__invoke") && it.contains("FindUserHandler") }
        assertNotNull("Query handler method FQN should be indexed, got keys: $keys", handlerMethodFqn)

        val values = FileBasedIndex.getInstance().getValues(CqrsQueryHandlerIndex.key, handlerMethodFqn!!, scope)
        assertTrue(
            "Indexed query FQN should resolve to FindUserQuery, got: $values",
            values.any { it.endsWith("\\FindUserQuery") }
        )
    }

    fun testEmptyValueIsNotIndexedWhenQueryTypeIsAbsent() {
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

        val scope = GlobalSearchScope.allScope(project)
        val keys = FileBasedIndex.getInstance().getAllKeys(CqrsQueryHandlerIndex.key, project)
        for (k in keys) {
            val values = FileBasedIndex.getInstance().getValues(CqrsQueryHandlerIndex.key, k, scope)
            assertFalse(
                "Empty-string value must never be indexed (key=$k, values=$values)",
                values.any { it.isEmpty() }
            )
        }
    }
}
