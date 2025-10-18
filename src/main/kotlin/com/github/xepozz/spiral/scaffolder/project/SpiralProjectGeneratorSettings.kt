package com.github.xepozz.spiral.scaffolder.project

data class SpiralProjectGeneratorSettings(
    var version: String = "latest",
    var createGit: Boolean = true,
    var template: String = "spiral/app",
)