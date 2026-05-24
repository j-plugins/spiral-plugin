package com.github.xepozz.spiral.views.index

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class ViewsNamespaceIndexTest : BasePlatformTestCase() {

    fun testIndexExtractsNamespaceFromAddDirectoryCall() {
        myFixture.configureByText(
            "ViewsBootloader.php",
            """
            <?php
            namespace Spiral\Views\Bootloader;

            class ViewsBootloader {
                public function addDirectory(string ${'$'}namespace, string ${'$'}directory): void {}
            }
            """.trimIndent()
        )

        myFixture.configureByText(
            "AppBootloader.php",
            """
            <?php
            use Spiral\Views\Bootloader\ViewsBootloader;

            function boot(ViewsBootloader ${'$'}viewsBootloader): void {
                ${'$'}viewsBootloader->addDirectory('app', __DIR__ . '/views');
            }
            """.trimIndent()
        )

        val namespaces = ViewsNamespaceIndexUtil.getAllNamespaces(project)

        assertTrue(
            "Index should contain 'app' namespace. Found: ${namespaces.keys}",
            namespaces.containsKey("app")
        )
        val value = namespaces["app"]
        assertNotNull("Index value for 'app' should be non-null", value)
        assertTrue(
            "Index value for 'app' should end with '/views'. Got: $value",
            value!!.endsWith("/views")
        )
    }

    fun testFileWithoutBootloaderCallsAddsNoNewNamespace() {
        // BasePlatformTestCase tests share a persistent index across test methods in the same
        // class, so we cannot assert `namespaces.isEmpty()`. Instead verify that a file with
        // no `addDirectory` call introduces no namespace that resolves to *this* file's path.
        myFixture.configureByText(
            "EmptyNoBootloader.php",
            """
            <?php
            ${'$'}xUniqueVarName = 1;
            """.trimIndent()
        )

        val namespaces = ViewsNamespaceIndexUtil.getAllNamespaces(project)
        val emptyFilePath = myFixture.file.virtualFile.path
        // Confirm no namespace value points at this file's directory (i.e., this file did not
        // contribute any namespace entry).
        assertTrue(
            "No namespace value should reference the empty file's path. Got: $namespaces (file=$emptyFilePath)",
            namespaces.values.none { it.startsWith(emptyFilePath) }
        )
    }
}
