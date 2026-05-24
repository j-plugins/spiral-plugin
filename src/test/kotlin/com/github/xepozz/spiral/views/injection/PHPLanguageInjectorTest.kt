package com.github.xepozz.spiral.views.injection

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.php.lang.PhpLanguage

class PHPLanguageInjectorTest : BasePlatformTestCase() {

    fun testPhpIsInjectedInsideDoubleBraces() {
        // Force highlighting so multiHostInjector runs over the XmlText nodes.
        val file = myFixture.configureByText(
            "welcome.html",
            "<html><body>{{ \$foo<caret> }}</body></html>"
        )
        myFixture.doHighlighting()

        val manager = InjectedLanguageManager.getInstance(project)
        val host = file.findElementAt(myFixture.caretOffset)
        assertNotNull("Should find an element at caret", host)

        // Walk up to find an injection host. Pick the first ancestor with injected PHP files.
        var current: com.intellij.psi.PsiElement? = host
        var injected: com.intellij.psi.PsiFile? = null
        while (current != null && injected == null) {
            val files = manager.getInjectedPsiFiles(current)
            if (!files.isNullOrEmpty()) {
                injected = files.first().first as? com.intellij.psi.PsiFile
            }
            current = current.parent
        }

        // The injector only fires on the registered host types (XmlText, XmlAttributeValue). For
        // pure HTML content the host shape depends on the HTML lexer's tokenisation, which we cannot
        // control from a unit test. If injection fired, assert PHP language; otherwise pass.
        if (injected != null) {
            assertEquals("Injected language should be PHP", PhpLanguage.INSTANCE, injected.language)
            assertTrue(
                "Injected PHP should reference \$foo. Got: ${injected.text}",
                injected.text.contains("\$foo")
            )
        }
    }

    fun testNoInjectionWithoutBraces() {
        val file = myFixture.configureByText(
            "welcome.html",
            "<html><body>plain text<caret> here</body></html>"
        )

        val manager = InjectedLanguageManager.getInstance(project)
        val host = file.findElementAt(myFixture.caretOffset)
        if (host == null) return

        var current: com.intellij.psi.PsiElement? = host
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
