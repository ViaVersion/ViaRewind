plugins {
    id("via.addon_subproject")
}

dependencies {
    compileOnly(projects.viarewindCommon)
    compileOnly(libs.velocity.api)
    annotationProcessor(libs.velocity.api)
}
