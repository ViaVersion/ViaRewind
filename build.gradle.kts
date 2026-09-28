import de.florianreuth.baseproject.viaRelease

plugins {
    `java-library`
    alias(libs.plugins.hangar.publish)
    alias(libs.plugins.minotaur)
    id("base.java")
    id("via.maven_publish")
    id("configuration.embedded_dependencies")
}

dependencies {
    embeddedDependencies(projects.viarewindCommon)
    embeddedDependencies(projects.viarewindBukkit)
    embeddedDependencies(projects.viarewindFabric)
    embeddedDependencies(projects.viarewindSponge)
    embeddedDependencies(projects.viarewindVelocity)
}

tasks {
    jar {
        manifest {
            attributes("paperweight-mappings-namespace" to "mojang")
        }
    }
}

val release = viaRelease("master")
if (!release.isRelease || release.isMainBranch) { // Only publish releases from the main branch
    modrinth {
        val mcVersions: List<String> = (property("minecraft_versions") as String)
            .split(",")
            .map { it.trim() }
        token.set(System.getenv("MODRINTH_TOKEN"))
        projectId.set("viarewind")
        versionType.set(release.modrinthVersionType)
        versionNumber.set(release.version)
        versionName.set(release.version)
        changelog.set(release.changelog)
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
            version.set(release.version)
            id.set("ViaRewind")
            channel.set(release.hangarChannel)
            changelog.set(release.changelog)
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
