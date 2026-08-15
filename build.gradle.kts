plugins {
    java
    id("com.gradleup.shadow") version "9.6.1"
}

group="net.onebeastchris.extension.magicmenu"
version="1.0.4"

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
    compileOnly("org.geysermc.geyser:api:2.11.1-SNAPSHOT")

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
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
}
