dependencies {
    compileOnly(project(":viarewind-common"))
    compileOnly("com.velocitypowered:velocity-api:3.4.0")?.also { annotationProcessor(it) }
}
