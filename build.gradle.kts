import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    `maven-publish`
    id("java")
}


group = "io.flamingock"
version = getFlamingockReleasedVersion()

repositories {
    mavenCentral()
    mavenLocal()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
        }
    }
    repositories {
        mavenLocal()
    }
}

val jacksonVersion = "2.15.2"
val flamingockVersion = "latest.release"
dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.flamingock:flamingock-core-api:$flamingockVersion")

    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}


java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

gradlePlugin {
    plugins {
        create("autoConfigurePlugin") {
            id = "io.flamingock.MetadataBundler"
            implementationClass = "io.flamingock.metadata.MetadataBundlerPlugin"
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

fun getFlamingockReleasedVersion(): String {
    val metadataUrl = "https://repo.maven.apache.org/maven2/io/flamingock/flamingock-core/maven-metadata.xml"
    try {
        val metadata = URL(metadataUrl).readText()
        val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val inputStream = metadata.byteInputStream()
        val document = documentBuilder.parse(inputStream)
        return document.getElementsByTagName("latest").item(0).textContent
    } catch (e: Exception) {
        throw RuntimeException("Cannot obtain Flamingock's latest version")
    }
}