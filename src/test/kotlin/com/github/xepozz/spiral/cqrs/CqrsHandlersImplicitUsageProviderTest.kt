package com.github.xepozz.spiral.cqrs

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.php.lang.PhpFileType
import com.jetbrains.php.lang.psi.elements.PhpClass

class CqrsHandlersImplicitUsageProviderTest : BasePlatformTestCase() {

    fun testCommandHandlerClassIsImplicitlyUsed() {
        val psiFile = myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
            <?php
            namespace Spiral\Cqrs\Attribute;
            #[\Attribute] class CommandHandler {}
            """.trimIndent()
        )
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
            <?php
            namespace App\Handler;
            use Spiral\Cqrs\Attribute\CommandHandler;

            class CreateUserHandler {
                #[CommandHandler]
                public function __invoke(${'$'}command): void {}
            }
            """.trimIndent()
        )
        val provider = CqrsHandlersImplicitUsageProvider()
        val phpFile = myFixture.file
        val handlerClass = PsiTreeUtil.findChildrenOfType(phpFile, PhpClass::class.java)
            .firstOrNull { it.name == "CreateUserHandler" }
        assertNotNull("Expected to find CreateUserHandler PsiClass", handlerClass)
        assertTrue("Handler class should be reported as implicit usage", provider.isImplicitUsage(handlerClass!!))
        assertTrue(
            "Handler class should be reported as having customized initialization",
            provider.isClassWithCustomizedInitialization(handlerClass)
        )
    }

    fun testPlainClassIsNotImplicitlyUsed() {
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
            <?php
            namespace App\Service;
            class PlainService {}
            """.trimIndent()
        )
        val provider = CqrsHandlersImplicitUsageProvider()
        val phpFile = myFixture.file
        val cls = PsiTreeUtil.findChildrenOfType(phpFile, PhpClass::class.java)
            .firstOrNull { it.name == "PlainService" }
        assertNotNull("Expected to find PlainService PsiClass", cls)
        assertFalse("Plain class must not be reported as implicit usage", provider.isImplicitUsage(cls!!))
        assertFalse(
            "Plain class must not be reported as having customized initialization",
            provider.isClassWithCustomizedInitialization(cls)
        )
    }
}
