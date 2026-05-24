package com.github.xepozz.spiral.forms

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.php.lang.psi.elements.Field
import com.jetbrains.php.lang.psi.elements.PhpClass

class FilterAttributesImplicitUsageProviderTest : BasePlatformTestCase() {

    private fun field(name: String): Field {
        return PsiTreeUtil.findChildrenOfType(myFixture.file, PhpClass::class.java)
            .flatMap { it.fields.toList() }
            .first { it.name == name }
    }

    fun testFieldOfAttributesFilterSubclassIsImplicitlyUsed() {
        myFixture.configureByText(
            "AttributesFilter.php",
            """
            <?php
            namespace Spiral\Validation\Symfony;
            class AttributesFilter {}
            """.trimIndent()
        )
        myFixture.configureByText(
            "UserFilter.php",
            """
            <?php
            namespace App\Filter;
            use Spiral\Validation\Symfony\AttributesFilter;

            class UserFilter extends AttributesFilter {
                public string ${'$'}name = '';
            }
            """.trimIndent()
        )

        val provider = FilterAttributesImplicitUsageProvider()
        val nameField = field("name")
        assertTrue("isImplicitUsage should be true", provider.isImplicitUsage(nameField))
        assertTrue("isImplicitRead should be true", provider.isImplicitRead(nameField))
        assertTrue("isImplicitWrite should be true", provider.isImplicitWrite(nameField))
    }

    fun testFieldOfUnrelatedClassIsNotImplicitlyUsed() {
        myFixture.configureByText(
            "Plain.php",
            """
            <?php
            namespace App;
            class Plain {
                public string ${'$'}foo = '';
            }
            """.trimIndent()
        )

        val provider = FilterAttributesImplicitUsageProvider()
        val fooField = field("foo")
        assertFalse(provider.isImplicitUsage(fooField))
        assertFalse(provider.isImplicitRead(fooField))
        assertFalse(provider.isImplicitWrite(fooField))
    }
}
