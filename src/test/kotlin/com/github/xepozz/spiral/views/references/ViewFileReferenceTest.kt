package com.github.xepozz.spiral.views.references

import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class ViewFileReferenceTest : BasePlatformTestCase() {

    private fun configureViewsInterface() {
        myFixture.configureByText(
            "ViewsInterface.php",
            """
            <?php
            namespace Spiral\Views;

            interface ViewsInterface {
                public function render(string ${'$'}path, array ${'$'}data = []): string;
            }
            """.trimIndent()
        )
    }

    fun testResolveViewWithoutNamespace() {
        configureViewsInterface()
        myFixture.addFileToProject("app/views/welcome.dark.php", "<html><body>Hello</body></html>")

        val file = myFixture.configureByText(
            "Render.php",
            """
            <?php
            use Spiral\Views\ViewsInterface;

            function go(ViewsInterface ${'$'}views): void {
                ${'$'}views->render('welco<caret>me');
            }
            """.trimIndent()
        )

        val ref = file.findReferenceAt(myFixture.caretOffset)
        assertNotNull("Should find a reference at caret", ref)
        val resolved = ref!!.resolve()
        assertNotNull("Reference should resolve to the view file", resolved)
        assertTrue(
            "Should resolve to a PsiFile",
            resolved is PsiFile
        )
        assertEquals("welcome.dark.php", (resolved as PsiFile).name)
    }

    fun testUnresolvedViewReturnsNull() {
        configureViewsInterface()

        val file = myFixture.configureByText(
            "Render.php",
            """
            <?php
            use Spiral\Views\ViewsInterface;

            function go(ViewsInterface ${'$'}views): void {
                ${'$'}views->render('nonex<caret>istent');
            }
            """.trimIndent()
        )

        val ref = file.findReferenceAt(myFixture.caretOffset)
        // Reference should still be created (soft reference), but resolve() should return null
        if (ref != null) {
            assertNull("Reference for nonexistent view should not resolve", ref.resolve())
        }
    }
}
