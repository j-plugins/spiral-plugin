package com.github.xepozz.spiral.console.run

import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Verifies that each persistent property of `SpiralConsoleCommandRunConfigurationSettings`
 * is backed by its own state delegate. A previous bug aliased `workingDirectory` (and
 * `documentRoot`) onto the `binary` attribute, causing one setter to silently overwrite
 * an unrelated property.
 */
class SpiralConsoleCommandRunConfigurationSettingsTest : BasePlatformTestCase() {

    fun testCommandNameRoundTrip() {
        val settings = SpiralConsoleCommandRunConfigurationSettings()
        assertEquals("", settings.commandName)

        settings.commandName = "migrate:init"
        assertEquals("migrate:init", settings.commandName)
    }

    fun testBinaryRoundTripPreservesDefault() {
        val settings = SpiralConsoleCommandRunConfigurationSettings()
        assertEquals("./app.php", settings.binary)

        settings.binary = "/usr/bin/php"
        assertEquals("/usr/bin/php", settings.binary)
    }

    fun testWorkingDirectoryIsIndependentFromBinary() {
        val settings = SpiralConsoleCommandRunConfigurationSettings().apply {
            binary = "./app.php"
            workingDirectory = "/tmp/spiral-app"
        }

        // Regression test for the historical bug where `workingDirectory` shared the
        // "binary" XML attribute. Setting one must not affect the other.
        assertEquals("./app.php", settings.binary)
        assertEquals("/tmp/spiral-app", settings.workingDirectory)
    }

    fun testDocumentRootIsIndependentFromBinary() {
        val settings = SpiralConsoleCommandRunConfigurationSettings().apply {
            binary = "./app.php"
            documentRoot = "/public"
        }

        assertEquals("./app.php", settings.binary)
        assertEquals("/public", settings.documentRoot)
    }

    fun testMutatingWorkingDirectoryDoesNotMutateBinary() {
        val settings = SpiralConsoleCommandRunConfigurationSettings()
        val originalBinary = settings.binary

        settings.workingDirectory = "/var/www/spiral"

        // Confirms that the working-dir setter writes to its own backing slot.
        assertEquals(originalBinary, settings.binary)
    }

    fun testMutatingDocumentRootDoesNotMutateBinary() {
        val settings = SpiralConsoleCommandRunConfigurationSettings()
        val originalBinary = settings.binary

        settings.documentRoot = "/public"

        assertEquals(originalBinary, settings.binary)
    }

    fun testDefaults() {
        val settings = SpiralConsoleCommandRunConfigurationSettings()
        assertEquals("", settings.commandName)
        assertEquals("./app.php", settings.binary)
        assertEquals("", settings.documentRoot)
        assertEquals("", settings.workingDirectory)
    }
}
