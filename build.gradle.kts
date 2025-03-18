import com.google.protobuf.gradle.*

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.23"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.23"
    id("org.jetbrains.intellij") version "1.17.3"
    id("com.google.protobuf") version "0.9.4"
}

group = "ai.codemaker.jetbrains"
version = "1.61.0-SNAPSHOT"

repositories {
    mavenCentral()
}

configurations.all {
    exclude(group = "org.slf4j")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    implementation("io.grpc:grpc-protobuf:1.64.0")
    implementation("io.grpc:grpc-services:1.64.0")
    implementation("io.grpc:grpc-stub:1.64.0")

    implementation("com.google.protobuf:protobuf-java-util:4.27.0")
    implementation("com.google.protobuf:protobuf-java:4.27.0")

    implementation("io.undertow:undertow-core:2.3.18.Final")

    runtimeOnly("io.grpc:grpc-netty-shaded:1.64.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.12.0")
}

tasks {
    test {
        useJUnitPlatform()
    }
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2023.1")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf(/* Plugin Dependencies */))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("231")
        untilBuild.set("241.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.27.0"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.64.0"
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                id("grpc") { }
            }
        }
    }
}