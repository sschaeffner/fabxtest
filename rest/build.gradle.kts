val ktorVersion: String by rootProject
val logbackVersion: String by rootProject

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.5.31"
}

dependencies {
    implementation(project(":domain"))

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-serialization:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation(testFixtures(project(":domain")))
}