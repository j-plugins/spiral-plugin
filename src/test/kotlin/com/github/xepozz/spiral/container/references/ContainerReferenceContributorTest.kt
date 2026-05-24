package com.github.xepozz.spiral.container.references

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.php.lang.psi.elements.Parameter

class ContainerReferenceContributorTest : BasePlatformTestCase() {

    fun testAutowireArrayKeyResolvesToConstructorParameter() {
        myFixture.configureByText(
            "Autowire.php",
            """
            <?php
            namespace Spiral\Core\Container;
            class Autowire {
                public function __construct(string ${'$'}class, array ${'$'}parameters = []) {}
            }
            """.trimIndent()
        )
        myFixture.configureByText(
            "MyService.php",
            """
            <?php
            namespace App;
            class MyService {
                public function __construct(public string ${'$'}name, public int ${'$'}count) {}
            }
            """.trimIndent()
        )
        myFixture.configureByText(
            "binding.php",
            """
            <?php
            use Spiral\Core\Container\Autowire;
            use App\MyService;

            ${'$'}wire = new Autowire(MyService::class, [
                'na<caret>me' => 'hello',
            ]);
            """.trimIndent()
        )

        val reference = myFixture.file.findReferenceAt(myFixture.caretOffset)
        assertNotNull("Expected a reference at the array key string literal", reference)
        val resolved = reference!!.resolve()
        assertNotNull("Expected the reference to resolve to a constructor parameter", resolved)
        assertTrue(
            "Expected resolved element to be a PHP Parameter, got: ${resolved!!.javaClass}",
            resolved is Parameter
        )
        assertEquals("name", (resolved as Parameter).name)
    }

    fun testNonAutowireNewExpressionDoesNotResolve() {
        myFixture.configureByText(
            "OtherWrapper.php",
            """
            <?php
            namespace App;
            class OtherWrapper {
                public function __construct(string ${'$'}class, array ${'$'}parameters = []) {}
            }
            """.trimIndent()
        )
        myFixture.configureByText(
            "MyService.php",
            """
            <?php
            namespace App;
            class MyService {
                public function __construct(public string ${'$'}name) {}
            }
            """.trimIndent()
        )
        myFixture.configureByText(
            "binding.php",
            """
            <?php
            use App\OtherWrapper;
            use App\MyService;

            ${'$'}wire = new OtherWrapper(MyService::class, [
                'na<caret>me' => 'hello',
            ]);
            """.trimIndent()
        )

        val reference = myFixture.file.findReferenceAt(myFixture.caretOffset)
        // Pattern only fires on Autowire; ensure no constructor-parameter reference appears.
        assertTrue(
            "Expected no constructor-parameter reference for non-Autowire wrapper",
            reference == null || reference !is ArrayConstructorParametersReference
        )
    }
}
