package com.github.xepozz.spiral.router.endpoints

import com.intellij.openapi.project.Project

data class SpiralGroup(
    val project: Project,
    val group: String,
    val routes: Collection<SpiralEndpoint>
)