package com.github.xepozz.spiral.php

import com.github.xepozz.spiral.SpiralFrameworkClasses
import com.intellij.openapi.util.text.StringUtil
import com.jetbrains.php.lang.psi.elements.PhpClass


fun PhpClass.hasTrait(fqn: String): Boolean = traits.any { it.fqn == fqn }


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