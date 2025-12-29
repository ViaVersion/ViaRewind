import de.florianmichael.baseproject.*

plugins {
    `java-library`
    id("io.papermc.hangar-publish-plugin")
    id("com.modrinth.minotaur")
    id("de.florianmichael.baseproject.BaseProject")
}

allprojects {

    setupProject()
    setupViaPublishing()

    repositories {
        mavenCentral()
        maven("https://repo.viaversion.com")
        maven("https://repo.papermc.io/repository/maven-public")
        maven("https://maven.fabricmc.net")
    }

}

subprojects {

    dependencies {
        compileOnly("com.viaversion:viaversion:5.7.0")
        compileOnly("com.viaversion:viabackwards:5.7.0")
    }

    tasks {
        processResources {
            val projectVersion = project.version
            val projectDescription = project.description
            filesMatching(listOf("plugin.yml", "fabric.mod.json", "META-INF/sponge_plugins.json")) {
                expand(mapOf("version" to projectVersion, "description" to projectDescription))
            }
        }
    }

}

base {
    archivesName.set("ViaRewind")
}

val shade = configureShadedDependencies(false) // Only shade, don't add them as dependency

dependencies {
    subprojects.forEach {
        shade(it)
    }
}

tasks {
    jar {
        manifest {
            attributes("paperweight-mappings-namespace" to "mojang")
        }
    }
}

val branch = branchName()
val baseVersion = version as String
val isRelease = !baseVersion.contains('-')
val isMainBranch = branch == "master"
if (!isRelease || isMainBranch) { // Only publish releases from the main branch
    val suffixedVersion = if (isRelease) baseVersion else baseVersion + "+" + System.getenv("GITHUB_RUN_NUMBER")
    val changelogContent = if (isRelease) {
        "See [GitHub](https://github.com/ViaVersion/ViaRewind) for release notes."
    } else {
        val commitHash = latestCommitHash()
        "[$commitHash](https://github.com/ViaVersion/ViaRewind/commit/$commitHash) ${latestCommitMessage()}"
    }

    modrinth {
        val mcVersions: List<String> = (property("minecraft_versions") as String)
            .split(",")
            .map { it.trim() }
        token.set(System.getenv("MODRINTH_TOKEN"))
        projectId.set("viarewind")
        versionType.set(if (isRelease) "release" else if (isMainBranch) "beta" else "alpha")
        versionNumber.set(suffixedVersion)
        versionName.set(suffixedVersion)
        changelog.set(changelogContent)
        uploadFile.set(tasks.jar.flatMap { it.archiveFile })
        gameVersions.set(mcVersions)
        loaders.add("fabric")
        loaders.add("paper")
        loaders.add("folia")
        loaders.add("velocity")
        autoAddDependsOn.set(false)
        detectLoaders.set(false)
        dependencies {
            required.project("viaversion")
            required.project("viabackwards")
            optional.project("viafabric")
        }
    }
    tasks.modrinth {
        notCompatibleWithConfigurationCache("")
    }

    hangarPublish {
        publications.register("plugin") {
            version.set(suffixedVersion)
            id.set("ViaRewind")
            channel.set(if (isRelease) "Release" else if (isMainBranch) "Snapshot" else "Alpha")
            changelog.set(changelogContent)
            apiKey.set(System.getenv("HANGAR_TOKEN"))
            platforms {
                paper {
                    jar.set(tasks.jar.flatMap { it.archiveFile })
                    platformVersions.set(listOf(property("minecraft_version_range") as String))
                    dependencies {
                        hangar("ViaVersion") {
                            required = true
                        }
                        hangar("ViaBackwards") {
                            required = true
                        }
                    }
                }
                velocity {
                    jar.set(tasks.jar.flatMap { it.archiveFile })
                    platformVersions.set(listOf(property("velocity_version") as String))
                    dependencies {
                        hangar("ViaVersion") {
                            required = true
                        }
                        hangar("ViaBackwards") {
                            required = true
                        }
                    }
                }
            }
        }
    }
    tasks.named("publishPluginPublicationToHangar") {
        notCompatibleWithConfigurationCache("")
    }
}
