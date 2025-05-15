dependencies {
    compileOnly(project(":viarewind-common"))
    compileOnly("com.viaversion:viaversion-velocity:5.0.0") // Needed for logger wrapper
    compileOnly("com.velocitypowered:velocity-api:3.1.1")?.also { annotationProcessor(it) }
}
