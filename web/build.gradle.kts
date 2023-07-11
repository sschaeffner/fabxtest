plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.9.0"
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":logging"))

    implementation("io.ktor:ktor-server-core:2.3.2")
    implementation("io.ktor:ktor-server-core-jvm:2.3.2")
    implementation("io.ktor:ktor-server-netty:2.3.2")
    implementation("io.ktor:ktor-server-auth:2.3.2")
    implementation("io.ktor:ktor-server-auth-jwt:2.3.2")
    implementation("io.ktor:ktor-server-http-redirect:2.3.2")
    implementation("io.ktor:ktor-server-forwarded-header:2.3.2")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.2")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.2")
    implementation("io.ktor:ktor-server-cors:2.3.2")
    implementation("io.ktor:ktor-server-status-pages:2.3.2")
    implementation("io.ktor:ktor-server-websockets:2.3.2")
    implementation("io.ktor:ktor-server-call-logging:2.3.2")
    implementation("io.ktor:ktor-server-metrics-micrometer:2.3.2")
    implementation("io.ktor:ktor-server-metrics-micrometer-jvm:2.3.2")
    implementation("io.micrometer:micrometer-registry-prometheus:1.11.2")
    implementation("org.slf4j:slf4j-api:2.0.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.2")
    implementation("ch.qos.logback:logback-classic:1.4.8")
    implementation("com.webauthn4j:webauthn4j-core:0.21.3.RELEASE")

    testImplementation(testFixtures(project(":domain")))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.2")
    testImplementation("io.ktor:ktor-server-test-host:2.3.2")
    testImplementation("io.ktor:ktor-client-content-negotiation:2.3.2")
    testImplementation("io.ktor:ktor-serialization-kotlinx-json:2.3.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.2")
}
