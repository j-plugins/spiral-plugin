package com.github.xepozz.spiral.router.endpoints

data class SpiralEndpoint(
    val url: String,
    val method: String,
    val fqn: String,
    val group: String,
)