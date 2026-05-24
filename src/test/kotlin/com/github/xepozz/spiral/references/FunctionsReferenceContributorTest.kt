package com.github.xepozz.spiral.references

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.php.lang.PhpFileType

class FunctionsReferenceContributorTest : BasePlatformTestCase() {

    fun testReferenceCreatedForDirectoryCall() {
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
            <?php
            function directory(string ${'$'}name): string { return ''; }
            ${'$'}d = directory('vie<caret>ws');
            """.trimIndent()
        )

        val ref = myFixture.file.findReferenceAt(myFixture.caretOffset)
        assertNotNull("expected reference inside directory() argument", ref)
        assertInstanceOf(ref!!, DirectoryReference::class.java)
    }

    fun testNoDirectoryReferenceForOtherFunctions() {
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
            <?php
            ${'$'}d = strlen('vie<caret>ws');
            """.trimIndent()
        )

        val ref = myFixture.file.findReferenceAt(myFixture.caretOffset)

        // Pattern is narrowed by withName("directory"), so other function calls must not
        // produce a DirectoryReference for their string arguments.
        if (ref is DirectoryReference) {
            fail("DirectoryReference must not trigger for strlen() argument")
        }
    }

    fun testCompletionVariantsExposedAtDirectoryArgument() {
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
            <?php
            function directory(string ${'$'}name): string { return ''; }
            ${'$'}d = directory('<caret>');
            """.trimIndent()
        )

        myFixture.completeBasic()
        val lookups = myFixture.lookupElementStrings ?: emptyList()

        // The DirectoryReference contributes the predefined-dirs lookup at the empty arg.
        assertContainsElements(lookups, "public", "vendor", "views")
    }
}
