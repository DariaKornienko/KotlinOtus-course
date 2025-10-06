plugins {
    kotlin("jvm") version "2.2.10"
    kotlin("plugin.serialization") version "2.1.0"
    id("io.ktor.plugin") version "2.3.11"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val ktorVersion = "2.3.11"

dependencies {
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    implementation("io.ktor:ktor-client-encoding:$ktorVersion")

    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")

    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-serialization-gson:$ktorVersion")
    implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")

    implementation("ch.qos.logback:logback-classic:1.4.12")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    ///////

    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")

    ///

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")


    implementation("org.mongodb:mongodb-driver-kotlin-sync:5.5.1")
    implementation("redis.clients:jedis:6.0.0")
}

application {
    mainClass.set("MainKt")
}

ktor{
    fatJar {
        archiveFileName.set("app.jar")
    }
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}

