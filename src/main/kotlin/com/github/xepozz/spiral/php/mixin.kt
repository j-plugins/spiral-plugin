package com.github.xepozz.spiral.php

import com.jetbrains.php.lang.psi.elements.PhpClass


fun PhpClass.hasTrait(fqn: String): Boolean = traits.any { it.fqn == fqn }
