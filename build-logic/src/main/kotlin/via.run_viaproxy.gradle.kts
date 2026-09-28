// Task to quickly test/debug code changes using https://github.com/ViaVersion/ViaProxy
// For further instructions see the ViaProxy repository README
plugins {
    java
}

val viaProxy = configurations.create("viaProxy")

dependencies {
    addProvider<MinimalExternalModuleDependency, ExternalModuleDependency>(
        viaProxy.name,
        project.the<VersionCatalogsExtension>().named("libs").findLibrary("viaproxy").get()
    ) {
        isTransitive = false
    }
}

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
