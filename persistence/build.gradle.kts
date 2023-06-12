val kotlinxCoroutinesVersion: String by project
val exposedVersion: String by project
val hikariVersion: String by project
val postgresDriverVersion: String by project
val liquibaseVersion: String by project
val testcontainersVersion: String by project

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.8.22"
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":logging"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    implementation("com.zaxxer:HikariCP:$hikariVersion")
    implementation("org.postgresql:postgresql:$postgresDriverVersion")
    implementation("org.liquibase:liquibase-core:$liquibaseVersion")
    implementation("com.mattbertolini:liquibase-slf4j:5.0.0")
    testImplementation(testFixtures(project(":domain")))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinxCoroutinesVersion")
    testImplementation("org.testcontainers:testcontainers:$testcontainersVersion")
    testImplementation("org.testcontainers:postgresql:$testcontainersVersion")
}