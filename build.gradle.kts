plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group="net.onebeastchris.extension.magicmenu"
version="1.0.3"

repositories {
    mavenCentral()
    maven("https://repo.opencollab.dev/main")
    //maven("https://jitpack.io")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    //mavenLocal()
}

dependencies {
    // Geyser API - needed for all extensions
    compileOnly("org.geysermc.geyser:api:2.4.2-SNAPSHOT")

    // Include other dependencies here - e.g. for configuration.
    compileOnly("org.geysermc.geyser:core:2.4.2-SNAPSHOT") {
        isTransitive = false
    }

    implementation("com.fasterxml.jackson.core:jackson-annotations:2.15.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.15.2")
    implementation("com.fasterxml.jackson.core:jackson-core:2.15.2")
}

tasks {
    jar {
        dependsOn(shadowJar)
        archiveClassifier.set("unshaded")
    }

    shadowJar {
        archiveBaseName.set("MagicMenu")
        archiveClassifier.set("")

        relocate("com.fasterxml.jackson", "net.onebeastchris.extension.magicmenu.jackson")
        relocate("org.yaml", "net.onebeastchris.extension.magicmenu.yaml")
    }

}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
