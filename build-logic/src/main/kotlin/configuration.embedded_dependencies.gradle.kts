plugins {
    java
}

val embeddedDependencies = configurations.create("embeddedDependencies") {
    isCanBeResolved = true
    isCanBeConsumed = true
}

tasks.jar {
    dependsOn(embeddedDependencies)
    from({ embeddedDependencies.map { zipTree(it) } }) {
        exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
