import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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
    implementation(kotlin("reflect"))

    // TODO: Exclude one of org.slf4j:jcl-over-slf4j (1.7.22 and 25)

    implementation("org.obolibrary.robot:robot-core:1.7.1") {
        exclude("org.slf4j")
        exclude("net.sourceforge.owlapi")
    }

    implementation("org.obolibrary.robot:robot-command:1.7.1") {
        exclude("org.slf4j")
    }

    implementation("net.sourceforge.owlapi:owlapi-distribution:4.5.16")
    /*implementation("edu.stanford.protege:explanation-workbench:3.0.0") {
        exclude("org.slf4j")
    }
    implementation("net.sourceforge.owlapi:owlexplanation:2.0.0") {
        exclude("net.sourceforge.owlapi")
    }*/
    // This is what we need to run 1.8.0-SNAPSHOT from JARs
    /*implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("net.sourceforge.owlapi:owlexplanation:2.0.0") //OK
    implementation("net.sourceforge.owlapi:owlapi-distribution:4.5.6")
    implementation("net.sourceforge.owlapi:jfact:4.0.4")
    implementation("net.sourceforge.owlapi:org.semanticweb.hermit:1.3.8.413")
    implementation("org.semanticweb.elk:elk-owlapi:0.4.3")
    implementation("commons-cli:commons-cli:1.2")
    implementation("commons-io:commons-io:2.4")
    implementation("org.apache.jena:jena-arq:3.8.0")
    implementation("org.apache.poi:poi:3.15")
    implementation("org.apache.logging.log4j:log4j-core:2.13.3")
    implementation("org.geneontology:owl-diff_2.12:1.1.2")
    implementation("org.geneontology:obographs:0.2.1")
    implementation("org.geneontology:expression-materializing-reasoner:0.1.3")
    implementation("edu.stanford.protege:explanation-workbench:3.0.0") {
        exclude("org.slf4j")
    }
    implementation("com.google.code.gson:gson:2.8.0")
    implementation("com.fasterxml.jackson.core:jackson-core:2.9.3")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.9.3")*/


    api("org.jetbrains.kotlin:kotlin-scripting-jvm:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm-host:$kotlinVersion")

    api("io.github.microutils:kotlin-logging:1.12.0")
    api("org.slf4j:slf4j-simple:1.7.29")
    api("org.slf4j:slf4j-log4j12:1.7.29")
    testImplementation("junit", "junit", "4.12")
}

tasks {
    named<ShadowJar>("shadowJar") {
        minimize {
            //exclude(dependency("org.jetbrains.kotlin:.*"))
            exclude(dependency("org.jetbrains.kotlin:kotlin-reflect"))
            exclude(dependency("org.jetbrains.kotlin:kotlin-compiler-embeddable"))
            exclude(dependency("org.apache.logging.log4j:.*"))
            exclude(dependency("log4j:log4j:.*"))
            exclude(dependency("com.fasterxml.jackson.core:.*"))
            exclude(dependency("org.semanticweb.elk:.*"))
            exclude(dependency("net.sourceforge.owlapi:.*"))
            exclude(dependency("org.openrdf.sesame:.*"))
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
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    freeCompilerArgs = listOf("-Xinline-classes")
}