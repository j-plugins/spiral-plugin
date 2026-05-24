package com.github.xepozz.spiral

import com.intellij.spellchecker.BundledDictionaryProvider

private const val DICTIONARY_PATH = "/META-INF/spellcheck.dic"

class SpellcheckingDictionaryProvider : BundledDictionaryProvider {
    override fun getBundledDictionaries(): Array<String> = arrayOf(DICTIONARY_PATH)
}