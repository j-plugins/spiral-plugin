package com.github.xepozz.spiral.router.index

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.indexing.FileBasedIndex

class RouterNamesIndexTest : BasePlatformTestCase() {

    private val routeStub = """
        <?php
        namespace Spiral\Router\Annotation;

        #[\Attribute]
        class Route {
            public function __construct(
                public string ${'$'}uri,
                public ?string ${'$'}name = null,
                public array|string ${'$'}methods = '*',
                public ?string ${'$'}defaults = null,
                public ?string ${'$'}group = null,
            ) {}
        }
    """.trimIndent()

    fun testNamedRouteIsIndexedByName() {
        myFixture.configureByText("Route.php", routeStub)
        myFixture.configureByText(
            "UserController.php",
            """
            <?php
            namespace App\Controller;
            use Spiral\Router\Annotation\Route;

            class UserController {
                #[Route(uri: '/users', name: 'users.list', methods: 'GET')]
                public function list(): void {}
            }
            """.trimIndent()
        )

        val keys = FileBasedIndex.getInstance().getAllKeys(RouterNamesIndex.key, project)
        assertTrue(
            "Expected 'users.list' to appear in the names index, got: $keys",
            keys.contains("users.list"),
        )

        val values = FileBasedIndex.getInstance()
            .getValues(RouterNamesIndex.key, "users.list", GlobalSearchScope.allScope(project))
        assertEquals("Expected exactly one route for 'users.list'", 1, values.size)
        assertEquals("/users", values.single().uri)
    }

    fun testUnnamedRouteIsNotInNamesIndex() {
        myFixture.configureByText("Route.php", routeStub)
        myFixture.configureByText(
            "PingController.php",
            """
            <?php
            namespace App\Controller;
            use Spiral\Router\Annotation\Route;

            class PingController {
                #[Route(uri: '/ping', methods: 'GET')]
                public function ping(): void {}
            }
            """.trimIndent()
        )

        val keys = FileBasedIndex.getInstance().getAllKeys(RouterNamesIndex.key, project)
        assertFalse(
            "Unnamed routes should not produce empty-string keys, got: $keys",
            keys.contains(""),
        )
    }
}
