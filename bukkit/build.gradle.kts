plugins {
    id("via.addon_subproject")
}

dependencies {
    compileOnly(projects.viarewindCommon)
    compileOnly(libs.spigot.api)
}
