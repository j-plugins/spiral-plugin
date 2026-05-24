package com.github.xepozz.spiral.router.index

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class RouterUrlsIndexTest : BasePlatformTestCase() {

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

    fun testRoutesOnSameUriWithinFileArePreserved() {
        myFixture.configureByText("Route.php", routeStub)
        myFixture.configureByText(
            "UserController.php",
            """
            <?php
            namespace App\Controller;
            use Spiral\Router\Annotation\Route;

            class UserController {
                #[Route(uri: '/users', methods: 'GET', name: 'users.list')]
                public function list(): void {}

                #[Route(uri: '/users', methods: 'POST', name: 'users.create')]
                public function create(): void {}
            }
            """.trimIndent()
        )

        val routes = RouterIndexUtil.getAllRoutes(project)
        val usersRoutes = routes.filter { it.uri == "/users" }

        assertEquals(
            "Both GET and POST routes on /users should be indexed",
            2,
            usersRoutes.size,
        )
        assertTrue(
            "Expected a GET /users route",
            usersRoutes.any { it.methods.contains("GET") },
        )
        assertTrue(
            "Expected a POST /users route",
            usersRoutes.any { it.methods.contains("POST") },
        )
    }

    fun testEmptyUriRouteIsSkipped() {
        myFixture.configureByText("Route.php", routeStub)
        myFixture.configureByText(
            "BadController.php",
            """
            <?php
            namespace App\Controller;
            use Spiral\Router\Annotation\Route;

            class BadController {
                #[Route(uri: '', methods: 'GET')]
                public function nothing(): void {}
            }
            """.trimIndent()
        )

        val routes = RouterIndexUtil.getAllRoutes(project)
        assertTrue(
            "Routes with empty URI must not be indexed; got: $routes",
            routes.none { it.uri.isEmpty() },
        )
    }

    fun testGroupNullWhenAbsent() {
        myFixture.configureByText("Route.php", routeStub)
        myFixture.configureByText(
            "PingController.php",
            """
            <?php
            namespace App\Controller;
            use Spiral\Router\Annotation\Route;

            class PingController {
                #[Route(uri: '/ping', name: 'ping', methods: 'GET')]
                public function ping(): void {}
            }
            """.trimIndent()
        )

        val route = RouterIndexUtil.getAllRoutes(project).single { it.uri == "/ping" }
        // Group is not present and parseRoutes resolves it at positional index 4; with only
        // three named args the index has no arg at slot 4, so group resolves to null.
        assertNull("Group should be null when not provided", route.group)
    }
}
