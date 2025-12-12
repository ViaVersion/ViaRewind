pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        id("de.florianmichael.baseproject.BaseProject") version "1.2.6"
        id("io.papermc.hangar-publish-plugin") version "0.1.4"
        id("net.raphimc.class-token-replacer") version "1.1.7"
        id("com.modrinth.minotaur") version "2.+"
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "viarewind"

setupViaSubproject("common")
setupViaSubproject("bukkit")
setupViaSubproject("fabric")
setupViaSubproject("sponge")
setupViaSubproject("velocity")

fun setupViaSubproject(name: String) {
    include("viarewind-$name")
    project(":viarewind-$name").projectDir = file(name)
}
