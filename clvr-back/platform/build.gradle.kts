plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
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
    implementation(libs.ktor.server.sessions)

    testImplementation(libs.ktor.server.tests)
    testImplementation(libs.ktor.client.contentNegotiation)
    testImplementation(libs.ktor.client.websockets)
    testImplementation(libs.jupiter)
}