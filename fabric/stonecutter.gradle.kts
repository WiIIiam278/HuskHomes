plugins {
    id("dev.kikugie.stonecutter")
}

// The version fabric/src is written for; commit it in the 26.3 state (see README)
stonecutter active "26.3"

stonecutter parameters {
    // Sources use current Mojang names; these map renamed classes and methods back for older versions
    replacements.regex(current.parsed >= "1.21.11") {
        replace("\\bResourceLocation\\b" to "Identifier", "\\bIdentifier\\b" to "ResourceLocation")
        // ResourceKey#location() was renamed with ResourceLocation
        replace("\\.location\\(\\)" to ".identifier()", "\\.identifier\\(\\)" to ".location()")
    }
    replacements.string(current.parsed >= "1.21.2") {
        replace("DimensionTransition", "TeleportTransition")
    }
}
