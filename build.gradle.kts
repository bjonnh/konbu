import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.kotlin.dsl.*

plugins {
    java
    application
    id("com.github.johnrengelman.shadow") version "6.1.0"
    kotlin("jvm") version "1.4.10"
}

val kotlinVersion = "1.4.10"

group = "net.nprod"
version = "0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

application {
    mainClassName = "net.nprod.konbu.MainKt"
}

dependencies {
    api(kotlin("stdlib"))

    api("org.obolibrary.robot:robot-core:1.7.1") {
        exclude("org.slf4j")
    }
    api("org.obolibrary.robot:robot-command:1.7.1") {
        exclude("org.slf4j")
    }

    implementation(kotlin("script-runtime", kotlinVersion))
    implementation(kotlin("script-util", kotlinVersion))
    implementation(kotlin("compiler-embeddable", kotlinVersion))
    implementation(kotlin("scripting-compiler-embeddable", kotlinVersion))
    implementation("net.java.dev.jna:jna:5.6.0")

    api("io.github.microutils:kotlin-logging:1.12.0")
    api("org.slf4j:slf4j-simple:1.7.29")
    testImplementation("junit", "junit", "4.12")
}

tasks {
    named<ShadowJar>("shadowJar") {
        minimize {
            exclude(dependency("org.jetbrains.kotlin:.*"))
        }
        isZip64 = true
        archiveBaseName.set("konbu-shadow")
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to application.mainClassName))
        }
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}