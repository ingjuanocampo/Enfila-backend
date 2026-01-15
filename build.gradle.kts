val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val firebase_admin_version: String by project

plugins {
    kotlin("jvm") version "1.9.24"
    id("io.ktor.plugin") version "2.3.12"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.24"
    application
}

group = "com.ingjuanocampo.enfila.backend"
version = "1.0.0"

application {
    mainClass.set("com.ingjuanocampo.enfila.backend.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

// Dependency resolution (temporarily disabled)
// configurations.all {
//     resolutionStrategy {
//         force("com.google.guava:guava:32.1.1-jre")
//         force("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")
//     }
//     exclude(group = "com.google.guava", module = "listenablefuture")
// }

dependencies {
    // Ktor Server
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-server-config-yaml")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.ktor:ktor-server-cors")
    implementation("io.ktor:ktor-server-call-logging")
    implementation("io.ktor:ktor-server-status-pages")

    // Ktor Client (for Twilio)
    implementation("io.ktor:ktor-client-core")
    implementation("io.ktor:ktor-client-cio")
    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("io.ktor:ktor-client-logging")

    // Firebase Admin SDK (temporarily disabled for basic version)
    // implementation("com.google.firebase:firebase-admin:9.2.0") {
    //     exclude(group = "com.google.guava", module = "listenablefuture")
    //     exclude(group = "com.google.guava", module = "guava")
    // }
    
    // Explicitly add Guava to resolve conflicts (not needed without Firebase)
    // implementation("com.google.guava:guava:32.1.1-jre")

    // Database
    implementation("org.postgresql:postgresql:42.7.0")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.jetbrains.exposed:exposed-core:0.44.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.44.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.44.1")
    implementation("org.jetbrains.exposed:exposed-java-time:0.44.1")

    // Dependency Injection
    implementation("io.insert-koin:koin-ktor:3.5.1")
    implementation("io.insert-koin:koin-logger-slf4j:3.5.1")

    // Logging
    implementation("ch.qos.logback:logback-classic:$logback_version")

    // Testing
    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}
