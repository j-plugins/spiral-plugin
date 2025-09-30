package com.github.xepozz.spiral

object SpiralViewUtil {
    const val VIEW_SUFFIX = "dark.php"

    val PREDEFINED_DIRS = mapOf(
        "public" to "/public/",
        "vendor" to "/vendor/",
        "runtime" to "/runtime/",
        "cache" to "/runtime/cache/",
        "config" to "/app/config/",
        "resources" to "/app/resources/",
        "root" to "/",
        "app" to "/app/",
        "views" to "/app/views/",
    )
}