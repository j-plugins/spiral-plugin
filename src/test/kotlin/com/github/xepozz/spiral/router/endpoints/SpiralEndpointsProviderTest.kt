package com.github.xepozz.spiral.router.endpoints

import com.intellij.microservices.endpoints.ModuleEndpointsFilter
import com.intellij.openapi.module.ModuleManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class SpiralEndpointsProviderTest : BasePlatformTestCase() {

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

    private fun moduleFilter(): ModuleEndpointsFilter {
        val module = ModuleManager.getInstance(project).modules.first()
        return ModuleEndpointsFilter(module, false)
    }

    fun testGroupsAndEndpointsAreSurfaced() {
        myFixture.configureByText("Route.php", routeStub)
        myFixture.configureByText(
            "ApiController.php",
            """
            <?php
            namespace App\Controller;
            use Spiral\Router\Annotation\Route;

            class ApiController {
                #[Route(uri: '/users', name: 'users.list', methods: 'GET', group: 'api')]
                public function list(): void {}

                #[Route(uri: '/ping', methods: 'GET')]
                public function ping(): void {}
            }
            """.trimIndent()
        )

        val provider = SpiralEndpointsProvider()
        val groups = provider.getEndpointGroups(project, moduleFilter()).toList()

        val apiGroup = groups.singleOrNull { it.group == "api" }
        assertNotNull("Expected an 'api' group, got groups: ${groups.map { it.group }}", apiGroup)
        val apiRoute = apiGroup!!.routes.singleOrNull { it.url == "/users" }
        assertNotNull("Expected /users endpoint in 'api' group", apiRoute)
        assertEquals("GET", apiRoute!!.method)

        val rootGroup = groups.singleOrNull { it.group == "Root" }
        assertNotNull("Expected a 'Root' group for ungrouped routes", rootGroup)
        assertTrue(
            "Expected /ping route in Root group",
            rootGroup!!.routes.any { it.url == "/ping" },
        )

        assertTrue(
            "All endpoints from a group should be valid",
            apiGroup.routes.all { provider.isValidEndpoint(apiGroup, it) },
        )
    }

    fun testNavigationElementResolvesToMethod() {
        myFixture.configureByText("Route.php", routeStub)
        myFixture.configureByText(
            "UserController.php",
            """
            <?php
            namespace App\Controller;
            use Spiral\Router\Annotation\Route;

            class UserController {
                #[Route(uri: '/users', methods: 'GET')]
                public function list(): void {}
            }
            """.trimIndent()
        )

        val provider = SpiralEndpointsProvider()
        val groups = provider.getEndpointGroups(project, moduleFilter()).toList()
        val group = groups.single { it.routes.any { e -> e.url == "/users" } }
        val endpoint = group.routes.single { it.url == "/users" }

        val navElement = provider.getNavigationElement(group, endpoint)
        assertNotNull("Expected navigation element for /users", navElement)
    }

    fun testNullProjectGracefullyHandled() {
        val group = SpiralGroup(project, "api", emptyList())
        assertNotNull(group.projectOrNull)
        assertEquals("api", group.group)
    }
}
