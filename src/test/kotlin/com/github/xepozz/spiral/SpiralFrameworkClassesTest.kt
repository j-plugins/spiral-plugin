package com.github.xepozz.spiral

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SpiralFrameworkClassesTest {

    @Test
    fun envFunctionFqnHasLeadingBackslash() {
        assertTrue(
            "ENV_FUNCTION must start with backslash to match PhpClass.fqn convention",
            SpiralFrameworkClasses.ENV_FUNCTION.startsWith("\\"),
        )
    }

    @Test
    fun envFunctionFqnIsGlobalEnv() {
        assertEquals("\\env", SpiralFrameworkClasses.ENV_FUNCTION)
    }

    @Test
    fun autowireFqnIsCorrect() {
        assertEquals("\\Spiral\\Core\\Container\\Autowire", SpiralFrameworkClasses.AUTOWIRE)
    }

    @Test
    fun allConstantsStartWithBackslash() {
        val constants = listOf(
            SpiralFrameworkClasses.INJECTABLE_CONFIG,
            SpiralFrameworkClasses.DIRECTORIES_INTERFACE,
            SpiralFrameworkClasses.ENVIRONMENT_INTERFACE,
            SpiralFrameworkClasses.PROTOTYPED,
            SpiralFrameworkClasses.PROTOTYPE_TRAIT,
            SpiralFrameworkClasses.PROTOTYPE_BOOTLOADER,
            SpiralFrameworkClasses.VIEWS_BOOTLOADER,
            SpiralFrameworkClasses.VIEWS_INTERFACE,
            SpiralFrameworkClasses.AS_COMMAND,
            SpiralFrameworkClasses.ROUTE,
            SpiralFrameworkClasses.ATTRIBUTES_FILTER,
            SpiralFrameworkClasses.CQRS_COMMAND_HANDLER,
            SpiralFrameworkClasses.CQRS_COMMAND,
            SpiralFrameworkClasses.CQRS_QUERY_HANDLER,
            SpiralFrameworkClasses.CQRS_QUERY,
            SpiralFrameworkClasses.ENV_FUNCTION,
            SpiralFrameworkClasses.AUTOWIRE,
        )
        constants.forEach {
            assertTrue("FQN '$it' must start with leading backslash", it.startsWith("\\"))
        }
    }
}
