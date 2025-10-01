package com.github.xepozz.spiral

import com.intellij.spellchecker.BundledDictionaryProvider

class SpellcheckingDictionaryProvider : BundledDictionaryProvider {
    override fun getBundledDictionaries(): Array<String> = arrayOf("/META-INF/spellcheck.dic")
}