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
//                val attribute = element.parent as? XmlAttribute ?: return
//
//                if (!attribute.name.startsWith(':')) return
//
                val injectableHost = element as? PsiLanguageInjectionHost ?: return
//
                injectPhpConstruction(injectableHost, registrar)
//                registrar
//                    .startInjecting(PhpLanguage.INSTANCE ?: return)
//                    .addPlace("<?=", "?>", injectableHost, TextRange(0, injectableHost.textLength))
//                    .doneInjecting()
            }

            is HtmlTag -> {
                element.children
                    .mapNotNull { it as? HtmlRawTextImpl }
                    .forEach { child ->
                        injectIntoText(HtmlTextInjectionHostWrapper(child), registrar)
                    }
            }

            is XmlText -> {
//                println("element: ${element.text}, ${element.javaClass.name} ${element is PsiLanguageInjectionHost}")
                val injectableHost = element as? PsiLanguageInjectionHost ?: return
                injectIntoText(injectableHost, registrar)
            }
        }
    }

    private fun injectIntoText(
        element: PsiLanguageInjectionHost,
        registrar: MultiHostRegistrar
    ) {
        val children = element.node.children().toList()
            .filter { it is XmlToken }
            .apply { if (isEmpty()) return }
//        println("children: $children")

        val textRange: TextRange

        injectPhpConstruction(element, registrar)

        if (children.size < 3) {
            val text = children[0].text
            var openTag = tagsMap.keys.find { text.startsWith(it) } ?: return
            var closeTag = tagsMap[openTag]?.apply { if (text.endsWith(this)) return } ?: return

            textRange = TextRange(text.indexOf(openTag) + openTag.length, text.indexOf(closeTag))

            println("openTag1: ${openTag}, closeTag: ${closeTag}")
        } else {
            var openTag = children.find { tagsMap.containsKey(it.text) }?.psi ?: return
            var closeTag = children.find { it.text == tagsMap[openTag.text] }?.psi ?: return
            textRange = TextRange(openTag.textRangeInParent.endOffset, closeTag.startOffsetInParent)
            println("openTag2: ${openTag.text}, closeTag: ${closeTag.text}")
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
//        HtmlTag::class.java,
    )
}