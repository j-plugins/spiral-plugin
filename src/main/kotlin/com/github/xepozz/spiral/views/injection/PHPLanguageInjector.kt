package com.github.xepozz.spiral.views.injection

import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.lang.tree.util.children
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.html.HtmlTag
import com.intellij.psi.impl.source.html.HtmlRawTextImpl
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlText
import com.intellij.psi.xml.XmlToken
import com.intellij.util.text.findTextRange
import com.jetbrains.php.lang.PhpLanguage

class PHPLanguageInjector : MultiHostInjector {
    val tagsMap = mapOf(
        "{!!" to "!!}",
        "{{" to "}}",
    )

    override fun getLanguagesToInject(
        registrar: MultiHostRegistrar,
        element: PsiElement
    ) {
        when (element) {
            is XmlAttributeValue -> {
                val injectableHost = element as? PsiLanguageInjectionHost ?: return
                injectPhpConstruction(injectableHost, registrar)
            }

            is HtmlTag -> {
                element.children
                    .mapNotNull { it as? HtmlRawTextImpl }
                    .forEach { child ->
                        injectIntoText(HtmlTextInjectionHostWrapper(child), registrar)
                    }
            }

            is XmlText -> {
                val injectableHost = element as? PsiLanguageInjectionHost ?: return
                injectIntoText(injectableHost, registrar)
            }
        }
    }

    /**
     * Injects PHP into the body of a Spiral templating expression (`{{ ... }}` or `{!! ... !!}`)
     * inside an XML/HTML text host.
     *
     * Two code paths are required because XML lexers can either:
     *   - merge the entire `{{ expr }}` sequence into a single [XmlToken] (single-child path), or
     *   - split it across three tokens — the opening brace, the body, and the closing brace
     *     (multi-child path).
     *
     * Both paths produce a [TextRange] that points to the body between the open and close tags
     * relative to [element]'s text. The leading `@`-directive form is handled by
     * [injectPhpConstruction].
     */
    private fun injectIntoText(
        element: PsiLanguageInjectionHost,
        registrar: MultiHostRegistrar
    ) {
        val children = element.node.children().toList()
            .filter { it is XmlToken }
            .apply { if (isEmpty()) return }

        val textRange: TextRange

        injectPhpConstruction(element, registrar)

        if (children.size < 3) {
            // Single-token case: the whole `{{ ... }}` sits in one XmlToken.
            val text = children[0].text
            val openTag = tagsMap.keys.find { text.startsWith(it) } ?: return
            val closeTag = tagsMap[openTag]?.apply { if (text.endsWith(this)) return } ?: return

            val closeIndex = text.indexOf(closeTag)
            if (closeIndex < 0) return

            textRange = TextRange(text.indexOf(openTag) + openTag.length, closeIndex)
        } else {
            // Multi-token case: open brace, body, and close brace are separate XmlTokens.
            val openTag = children.find { tagsMap.containsKey(it.text) }?.psi ?: return
            val closeTag = children.find { it.text == tagsMap[openTag.text] }?.psi ?: return
            textRange = TextRange(openTag.textRangeInParent.endOffset, closeTag.startOffsetInParent)
        }

        registrar.startInjecting(PhpLanguage.INSTANCE)
            .addPlace("<?=", "?>", element, textRange)
            .doneInjecting()
    }

    private fun injectPhpConstruction(
        element: PsiLanguageInjectionHost,
        registrar: MultiHostRegistrar
    ) {
        val elementContent = when (element) {
            is XmlAttributeValue -> element.value
            else -> element.text
        }

        val text = elementContent.trim()
        if (!text.startsWith("@")) return

        val textRange = element.text.findTextRange(text) ?: return

        registrar.startInjecting(PhpLanguage.INSTANCE)
            .addPlace("<?php ", "?>", element, textRange.shiftRight(1).grown(-1))
            .doneInjecting()
    }

    override fun elementsToInjectIn() = listOf(
        XmlAttributeValue::class.java,
        XmlText::class.java,
    )
}