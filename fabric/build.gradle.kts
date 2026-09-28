plugins {
    id("via.addon_subproject")
}

dependencies {
    compileOnly(projects.viarewindCommon)
    compileOnly(libs.fabric.loader)
    compileOnly(libs.log4j.api)
}
