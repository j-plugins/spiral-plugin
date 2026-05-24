package com.github.xepozz.spiral.prototyped

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.php.lang.PhpFileType

class PrototypedCompletionTest : BasePlatformTestCase() {

    private fun configureWithPrototype() {
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
            <?php
            namespace Spiral\Prototype\Annotation;
            #[\Attribute]
            class Prototyped {
                public function __construct(public string ${'$'}property = '') {}
            }
            namespace Spiral\Prototype\Traits;
            trait PrototypeTrait {}
            namespace App\Logger;
            #[\Spiral\Prototype\Annotation\Prototyped(property: 'logger')]
            class LoggerService {}
            namespace App;
            class WithPrototype {
                use \Spiral\Prototype\Traits\PrototypeTrait;
                public function run(): void {
                    ${'$'}this-><caret>
                }
            }
            """.trimIndent()
        )
    }

    fun testCompletionContainsIndexedPrototypeProperty() {
        configureWithPrototype()

        myFixture.complete(CompletionType.BASIC)
        val lookups = myFixture.lookupElementStrings

        // Completion is only active for classes using PrototypeTrait; the indexed
        // prototype property `logger` must appear in the resulting lookup set.
        assertNotNull("completion should return suggestions", lookups)
        assertContainsElements(lookups!!, "logger")
    }

    fun testCompletionEmptyWhenTraitMissing() {
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
            <?php
            namespace App;
            class NoTrait {
                public function run(): void {
                    ${'$'}this-><caret>
                }
            }
            """.trimIndent()
        )

        myFixture.complete(CompletionType.BASIC)
        val lookups = myFixture.lookupElementStrings ?: emptyList()

        // Without PrototypeTrait the contributor must not add any prototype suggestions.
        assertFalse(
            "must not propose 'logger' when host class does not use PrototypeTrait",
            "logger" in lookups
        )
    }
}
