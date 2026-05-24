package com.github.xepozz.spiral.config.index

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class PrototypedIndexTest : BasePlatformTestCase() {

    fun testPrototypedAttributeIsIndexed() {
        myFixture.configureByText(
            "Prototyped.php",
            """
            <?php
            namespace Spiral\Prototype\Annotation;
            #[\Attribute(\Attribute::TARGET_CLASS)]
            class Prototyped {
                public function __construct(public ?string ${'$'}property = null) {}
            }
            """.trimIndent()
        )
        myFixture.configureByText(
            "UserService.php",
            """
            <?php
            namespace App\Service;
            use Spiral\Prototype\Annotation\Prototyped;

            #[Prototyped(property: "users")]
            class UserService {}
            """.trimIndent()
        )

        val prototypes = PrototypedIndex.getPrototypes(project)
        assertTrue(
            "Expected 'users' to be in indexed prototypes, got: $prototypes",
            prototypes.contains("users")
        )

        val fqn = PrototypedIndex.getPrototypeClass("users", project)
        assertNotNull("getPrototypeClass should resolve indexed FQN", fqn)
        assertTrue(
            "Expected FQN to point to UserService, got $fqn",
            fqn!!.endsWith("\\UserService")
        )
    }
}
