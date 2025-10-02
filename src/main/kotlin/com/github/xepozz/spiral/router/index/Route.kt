package com.github.xepozz.spiral.router.index

import java.io.Serializable

data class Route(
    val uri: String,
    val name: String?,
    val methods: Collection<String>,
    val fqn: String,
    val group: String?,
) : Serializable