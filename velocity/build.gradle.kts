dependencies {
    compileOnly(project(":viarewind-common"))
    compileOnly("com.velocitypowered:velocity-api:3.1.1")?.also { annotationProcessor(it) }
}
