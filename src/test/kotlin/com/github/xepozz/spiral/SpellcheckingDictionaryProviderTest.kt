package com.github.xepozz.spiral

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SpellcheckingDictionaryProviderTest {

    @Test
    fun providerReturnsExpectedDictionaryPath() {
        val provider = SpellcheckingDictionaryProvider()
        val dictionaries = provider.getBundledDictionaries()

        assertArrayEquals(arrayOf("/META-INF/spellcheck.dic"), dictionaries)
    }

    @Test
    fun providerReturnsNonEmptyDictionaries() {
        val provider = SpellcheckingDictionaryProvider()
        val dictionaries = provider.getBundledDictionaries()

        assertEquals(1, dictionaries.size)
        assertTrue("Dictionary path must reference a resource", dictionaries[0].startsWith("/"))
    }
}
