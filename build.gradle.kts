plugins {
    java
    kotlin("jvm") version "1.4.10"
}

group = "net.nprod"
version = "0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("org.obolibrary.robot:robot-core:1.7.0") {
        exclude ("org.slf4j")
    }
    implementation("org.obolibrary.robot:robot-command:1.7.0") {
        exclude ("org.slf4j")
    }


    implementation("io.github.microutils:kotlin-logging:1.12.0")
    implementation("org.slf4j:slf4j-simple:1.7.29")
    testImplementation("junit", "junit", "4.12")
}
