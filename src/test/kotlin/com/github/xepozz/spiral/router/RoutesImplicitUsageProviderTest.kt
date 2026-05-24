package com.github.xepozz.spiral.router

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.php.lang.psi.elements.PhpClass

class RoutesImplicitUsageProviderTest : BasePlatformTestCase() {

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

    private fun phpClass(name: String): PhpClass {
        return PsiTreeUtil.findChildrenOfType(myFixture.file, PhpClass::class.java)
            .first { it.name == name }
    }

    fun testControllerWithRouteAttributeIsImplicitlyUsed() {
        myFixture.configureByText("Route.php", routeStub)
        myFixture.configureByText(
            "RoutedController.php",
            """
            <?php
            namespace App\Controller;
            use Spiral\Router\Annotation\Route;

            class RoutedController {
                #[Route(uri: '/x', methods: 'GET')]
                public function action(): void {}
            }
            """.trimIndent()
        )

        val provider = RoutesImplicitUsageProvider()
        val controller = phpClass("RoutedController")
        assertTrue("Class with @Route on a method should be implicitly used", provider.isImplicitUsage(controller))
    }

    fun testClassWithoutRouteAttributeIsNotImplicitlyUsed() {
        myFixture.configureByText(
            "Plain.php",
            """
            <?php
            namespace App;
            class Plain {
                public function noop(): void {}
            }
            """.trimIndent()
        )

        val provider = RoutesImplicitUsageProvider()
        val plain = phpClass("Plain")
        assertFalse(provider.isImplicitUsage(plain))
    }
}
