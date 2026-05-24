package com.github.xepozz.spiral.references

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.php.lang.PhpFileType

class DirectoryReferenceTest : BasePlatformTestCase() {

    fun testDirectoryCallProducesDirectoryReference() {
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
            <?php
            function directory(string ${'$'}name): string { return ''; }
            ${'$'}d = directory('pu<caret>blic');
            """.trimIndent()
        )

        val ref = myFixture.file.findReferenceAt(myFixture.caretOffset)
            ?: fail("no reference at caret inside directory() argument") as Nothing

        assertInstanceOf(ref, DirectoryReference::class.java)
        assertEquals("public", (ref as DirectoryReference).directory)
    }

    fun testDirectoryReferenceVariantsContainPredefinedDirs() {
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
            <?php
            function directory(string ${'$'}name): string { return ''; }
            ${'$'}d = directory('pu<caret>blic');
            """.trimIndent()
        )

        val ref = myFixture.file.findReferenceAt(myFixture.caretOffset) as? DirectoryReference
            ?: fail("expected DirectoryReference") as Nothing

        val keys = ref.variants
            .mapNotNull { (it as? LookupElement)?.lookupString }
            .toSet()

        // PREDEFINED_DIRS must surface as completion variants of the directory() argument.
        assertContainsElements(keys, "public", "vendor", "runtime", "config", "views")
    }
}
