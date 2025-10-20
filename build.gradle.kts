plugins {
    java
}

group = "dev.crystalmath"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:24.1.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.jar {
    archiveClassifier.set("")
    duplicatesStrategy = org.gradle.api.file.DuplicatesStrategy.INCLUDE
}

// Paper doesn't require running tests by default
tasks.test {
    useJUnitPlatform()
    enabled = false
}
