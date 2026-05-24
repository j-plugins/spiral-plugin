package com.github.xepozz.spiral.views.injection

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.php.lang.PhpLanguage

class PHPLanguageInjectorTest : BasePlatformTestCase() {

    fun testPhpIsInjectedInsideDoubleBraces() {
        val file = myFixture.configureByText(
            "welcome.html",
            "<html><body>{{ \$foo<caret> }}</body></html>"
        )

        val manager = InjectedLanguageManager.getInstance(project)
        val hostLeaf = file.findElementAt(myFixture.caretOffset)
        assertNotNull("Should find an element at caret", hostLeaf)

        var current: PsiElement? = hostLeaf
        var injected: PsiFile? = null
        while (current != null && injected == null) {
            val files = manager.getInjectedPsiFiles(current)
            if (!files.isNullOrEmpty()) {
                injected = files.first().first as? PsiFile
            }
            current = current.parent
        }

        assertNotNull("Expected PHP to be injected into {{ ... }}", injected)
        assertEquals("Injected language should be PHP", PhpLanguage.INSTANCE, injected!!.language)
        assertTrue(
            "Injected PHP should reference \$foo. Got: ${injected.text}",
            injected.text.contains("\$foo")
        )
    }

    fun testNoInjectionWithoutBraces() {
        val file = myFixture.configureByText(
            "welcome.html",
            "<html><body>plain text<caret> here</body></html>"
        )

        val manager = InjectedLanguageManager.getInstance(project)
        val hostLeaf = file.findElementAt(myFixture.caretOffset) ?: return

        var current: PsiElement? = hostLeaf
        while (current != null) {
            val files = manager.getInjectedPsiFiles(current)
            assertTrue(
                "No PHP injection expected in plain text. Got: $files",
                files.isNullOrEmpty()
            )
            current = current.parent
        }
    }
}
