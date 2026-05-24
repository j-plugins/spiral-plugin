package com.github.xepozz.spiral

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class MyPluginTest : BasePlatformTestCase() {
    fun testPluginLoads() {
        assertNotNull(project)
    }
}
