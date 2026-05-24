package com.github.xepozz.spiral.config.index

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.indexing.FileBasedIndex

class ConfigSectionIndexTest : BasePlatformTestCase() {

    fun testInjectableConfigSubclassIsIndexed() {
        myFixture.configureByText(
            "InjectableConfig.php",
            """
            <?php
            namespace Spiral\Core;
            class InjectableConfig {}
            """.trimIndent()
        )
        myFixture.configureByText(
            "AppConfig.php",
            """
            <?php
            namespace App\Config;
            use Spiral\Core\InjectableConfig;
            class AppConfig extends InjectableConfig {}
            """.trimIndent()
        )

        val keys = FileBasedIndex.getInstance().getAllKeys(ConfigSectionIndex.key, project)
        assertTrue(
            "AppConfig FQN should be in the index keys, got: ${'$'}keys",
            keys.any { it.endsWith("\\AppConfig") || it == "\\App\\Config\\AppConfig" }
        )
    }

    fun testClassNotExtendingInjectableConfigIsNotIndexed() {
        myFixture.configureByText(
            "RandomClass.php",
            """
            <?php
            namespace App;
            class RandomClass {}
            """.trimIndent()
        )

        val scope = GlobalSearchScope.allScope(project)
        val keys = FileBasedIndex.getInstance().getAllKeys(ConfigSectionIndex.key, project)
        for (k in keys) {
            val values = FileBasedIndex.getInstance().getValues(ConfigSectionIndex.key, k, scope)
            assertTrue(values.none { it.endsWith("\\RandomClass") })
        }
    }
}
