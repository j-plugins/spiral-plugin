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

    fun testGetAllNamespacesReturnsEmptyWhenNoBootloaderCalls() {
        myFixture.configureByText(
            "Empty.php",
            """
            <?php
            ${'$'}x = 1;
            """.trimIndent()
        )

        val namespaces = ViewsNamespaceIndexUtil.getAllNamespaces(project)

        assertFalse(namespaces.containsKey("app"))
    }
}
