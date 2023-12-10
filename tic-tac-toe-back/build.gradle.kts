plugins {
    jacoco
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
}

group = "com.clvr.server"
version = "0.0.1"

application {
    mainClass.set("com.clvr.server.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.postgres)
    implementation(libs.h2)
    implementation(libs.kotlin.logging)
    implementation(libs.logback)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.contentNegotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.websockets)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.cors)
    implementation("io.ktor:ktor-server-sessions:2.3.5")


    testImplementation(libs.ktor.server.tests)
    testImplementation(libs.ktor.client.contentNegotiation)
    testImplementation(libs.ktor.client.websockets)
    testImplementation(libs.jupiter)
}

tasks.jacocoTestReport {
    reports {
        xml.required = true
        csv.required = true
    }
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}
