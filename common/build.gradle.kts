import de.florianmichael.baseproject.latestCommitHash

plugins {
    id("net.raphimc.class-token-replacer")
}

val viaProxy: Configuration by configurations.creating

dependencies {
    compileOnly("io.netty:netty-all:4.0.20.Final")
    compileOnly("com.google.guava:guava:17.0")

    viaProxy("net.raphimc:ViaProxy:[3.0.0,4.0.0)") {
        isTransitive = false
    }
}

sourceSets.configureEach {
    classTokenReplacer {
        replaceInPlace.set(true)

        property("\${version}", project.version)
        property("\${impl_version}", "git-ViaRewind-${project.version}:${latestCommitHash()}")
    }
}

// Task to quickly test/debug code changes using https://github.com/ViaVersion/ViaProxy
// For further instructions see the ViaProxy repository README
val prepareViaProxyFiles by tasks.registering(Copy::class) {
    dependsOn(project.tasks.jar)

    from(project.tasks.jar.get().archiveFile.get().asFile)
    into(layout.projectDirectory.dir("run/jars"))

    val projectName = project.name
    rename { "${projectName}.jar" }
}

val cleanupViaProxyFiles by tasks.registering(Delete::class) {
    delete(
        layout.projectDirectory.file("run/jars/${project.name}.jar"),
        layout.projectDirectory.dir("run/logs")
    )
}

tasks.register<JavaExec>("runViaProxy") {
    dependsOn(prepareViaProxyFiles)
    finalizedBy(cleanupViaProxyFiles)

    mainClass.set("net.raphimc.viaproxy.ViaProxy")
    classpath = viaProxy
    workingDir = layout.projectDirectory.dir("run").asFile
    jvmArgs = listOf("-DskipUpdateCheck")

    if (System.getProperty("viaproxy.gui.autoStart") != null) {
        jvmArgs("-Dviaproxy.gui.autoStart")
    }
}
