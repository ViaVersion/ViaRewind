import de.florianreuth.baseproject.latestCommitHash
import net.raphimc.classtokenreplacer.extension.ClassTokenReplacerExtension

plugins {
    java
    id("net.raphimc.class-token-replacer")
}

val implVersion = "git-${property("project_name")}-${project.version}:${latestCommitHash()}"
sourceSets.configureEach {
    extensions.getByType<ClassTokenReplacerExtension>().apply {
        property("\${version}", project.version)
        property("\${impl_version}", implVersion)
    }
}

// With replaceInPlace the plugin only finalizes classes with the replacement, so the jar could pack unreplaced classes
tasks.jar {
    dependsOn("replaceTokens")
}
