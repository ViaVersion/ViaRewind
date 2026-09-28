plugins {
    java
    id("base.java")
    id("via.maven_publish")
}

val libs = project.the<VersionCatalogsExtension>().named("libs")

dependencies {
    compileOnly(libs.findLibrary("viaversion").get())
    compileOnly(libs.findLibrary("viabackwards").get())
}

tasks.processResources {
    val projectVersion = project.version
    val projectDescription = project.description
    filesMatching(listOf("plugin.yml", "fabric.mod.json", "META-INF/sponge_plugins.json")) {
        expand(mapOf("version" to projectVersion, "description" to projectDescription))
    }
}
