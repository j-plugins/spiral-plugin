package com.github.xepozz.spiral

object SpiralFrameworkClasses {
    const val INJECTABLE_CONFIG = "\\Spiral\\Core\\InjectableConfig"
    const val DIRECTORIES_INTERFACE = "\\Spiral\\Boot\\DirectoriesInterface"
    const val ENVIRONMENT_INTERFACE = "\\Spiral\\Boot\\EnvironmentInterface"
    const val PROTOTYPED = "\\Spiral\\Prototype\\Annotation\\Prototyped"
    const val PROTOTYPE_TRAIT = "\\Spiral\\Prototype\\Traits\\PrototypeTrait"
    const val PROTOTYPE_BOOTLOADER = "\\Spiral\\Prototype\\Bootloader\\PrototypeBootloader"
    const val VIEWS_BOOTLOADER = "\\Spiral\\Views\\Bootloader\\ViewsBootloader"
    const val VIEWS_INTERFACE = "\\Spiral\\Views\\ViewsInterface"

    const val AS_COMMAND = "\\Spiral\\Console\\Attribute\\AsCommand"

    const val ROUTE = "\\Spiral\\Router\\Annotation\\Route"
    const val ATTRIBUTES_FILTER = "\\Spiral\\Validation\\Symfony\\AttributesFilter"

    const val CQRS_COMMAND_HANDLER = "\\Spiral\\Cqrs\\Attribute\\CommandHandler"
    const val CQRS_COMMAND = "\\Spiral\\Cqrs\\CommandInterface"
    const val CQRS_QUERY_HANDLER = "\\Spiral\\Cqrs\\Attribute\\QueryHandler"
    const val CQRS_QUERY = "\\Spiral\\Cqrs\\QueryInterface"
}