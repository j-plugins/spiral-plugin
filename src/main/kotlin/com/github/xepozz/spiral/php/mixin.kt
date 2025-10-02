package com.github.xepozz.spiral.php

import com.github.xepozz.spiral.SpiralFrameworkClasses
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.ElementManipulators
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression


fun PhpClass.hasTrait(fqn: String): Boolean = traits.any { it.fqn == fqn }
fun PhpClass.hasInterface(fqn: String): Boolean = implementedInterfaces.any { it.fqn == fqn }
fun PhpClass.hasSuperClass(fqn: String): Boolean = superClasses.any { it.fqn == fqn }
val StringLiteralExpression.contentRange: TextRange
    get() = ElementManipulators.getValueTextRange(this).shiftRight(textRange.startOffset)

fun PhpClass.getConsoleCommandName(): String? {
    return this
        .getAttributes(SpiralFrameworkClasses.AS_COMMAND)
        .firstOrNull()
        ?.arguments
        ?.run { this.find { it.name == "name" } ?: firstOrNull() }
        ?.argument
        ?.value
        ?.run { StringUtil.unquoteString(this) }
}