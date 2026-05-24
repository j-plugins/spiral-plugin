package com.github.xepozz.spiral.router.references

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.psi.PsiReference
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class RouterReferenceVariantsTest : BasePlatformTestCase() {

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

    private fun configureRoutes() {
        myFixture.configureByText("Route.php", routeStub)
        myFixture.configureByText(
            "RoutesController.php",
            """
            <?php
            namespace App\Controller;
            use Spiral\Router\Annotation\Route;

            class RoutesController {
                #[Route(uri: '/users', name: 'users.list', methods: 'GET', group: 'api')]
                public function list(): void {}

                #[Route(uri: '/orders', name: 'orders.list', methods: 'GET')]
                public function orders(): void {}
            }
            """.trimIndent()
        )
    }

    private fun lookupStrings(reference: PsiReference): List<String> {
        return reference.variants
            .mapNotNull { (it as? LookupElement)?.lookupString }
    }

    fun testNameReferenceVariantsContainIndexedNames() {
        configureRoutes()
        myFixture.configureByText(
            "consumer.php",
            """
            <?php
            use Spiral\Router\Annotation\Route;
            class C {
                #[Route(uri: '/x', name: '<caret>')]
                public function action(): void {}
            }
            """.trimIndent()
        )

        val reference = myFixture.file.findReferenceAt(myFixture.caretOffset)
        assertNotNull("Expected a reference at the name argument", reference)
        val names = lookupStrings(reference!!)
        assertTrue(
            "Expected 'users.list' in completion variants, got: $names",
            names.contains("users.list"),
        )
        assertTrue(
            "Expected 'orders.list' in completion variants, got: $names",
            names.contains("orders.list"),
        )
    }

    fun testUriReferenceVariantsContainIndexedUris() {
        configureRoutes()
        myFixture.configureByText(
            "consumer.php",
            """
            <?php
            use Spiral\Router\Annotation\Route;
            class C {
                #[Route(uri: '<caret>')]
                public function action(): void {}
            }
            """.trimIndent()
        )

        val reference = myFixture.file.findReferenceAt(myFixture.caretOffset)
        assertNotNull("Expected a reference at the uri argument", reference)
        val uris = lookupStrings(reference!!)
        assertTrue(
            "Expected '/users' in URI completion variants, got: $uris",
            uris.contains("/users"),
        )
    }

    fun testGroupReferenceVariantsContainIndexedGroupsOnly() {
        configureRoutes()
        myFixture.configureByText(
            "consumer.php",
            """
            <?php
            use Spiral\Router\Annotation\Route;
            class C {
                #[Route(uri: '/x', name: 'x', methods: 'GET', defaults: null, group: '<caret>')]
                public function action(): void {}
            }
            """.trimIndent()
        )

        val reference = myFixture.file.findReferenceAt(myFixture.caretOffset)
        assertNotNull("Expected a reference at the group argument", reference)
        val groups = lookupStrings(reference!!)
        assertTrue(
            "Expected 'api' in group completion variants, got: $groups",
            groups.contains("api"),
        )
        // Routes without an explicit group must not appear as a completion
        // entry (the empty/null group is implicit Root).
        assertFalse(
            "Empty group should not appear as a completion variant",
            groups.contains(""),
        )
    }

    fun testMethodsReferenceVariantsAreAllVerbs() {
        configureRoutes()
        myFixture.configureByText(
            "consumer.php",
            """
            <?php
            use Spiral\Router\Annotation\Route;
            class C {
                #[Route(uri: '/x', methods: '<caret>')]
                public function action(): void {}
            }
            """.trimIndent()
        )

        val reference = myFixture.file.findReferenceAt(myFixture.caretOffset)
        assertNotNull("Expected a reference at the methods argument", reference)
        val methods = lookupStrings(reference!!)
        assertTrue("Expected GET in verbs, got: $methods", methods.contains("GET"))
        assertTrue("Expected POST in verbs, got: $methods", methods.contains("POST"))
        assertTrue("Expected DELETE in verbs, got: $methods", methods.contains("DELETE"))
    }
}
