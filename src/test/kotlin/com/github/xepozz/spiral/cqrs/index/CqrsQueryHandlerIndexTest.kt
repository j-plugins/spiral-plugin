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

    fun testUntypedHandlerParameterIndexesEmptyValue() {
        // Current production contract: when a #[QueryHandler] method's first parameter has no
        // class type hint, the indexer still emits the method FQN as a key, with an empty-string
        // value (the unresolved query FQN). Skipping these would require a `getVersion()` bump
        // and is out of scope here.
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
        val untypedKey = keys.firstOrNull { it.endsWith(".__invoke") && it.contains("UntypedQueryHandler") }
        assertNotNull("UntypedQueryHandler.__invoke should still be indexed, got keys: $keys", untypedKey)
        val values = FileBasedIndex.getInstance().getValues(CqrsQueryHandlerIndex.key, untypedKey!!, scope)
        assertTrue(
            "Untyped query handler value is the empty string per current production indexer; got: $values",
            values.contains("")
        )
    }
}
