plugins {
    kotlin("jvm") version "2.0.0"
    id("com.google.devtools.ksp") version "2.0.0-1.0.22"
    id("application")
}


group = "ru.kingofraccoons"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("eu.vendeli:telegram-bot:6.2.0")
    ksp("eu.vendeli:ksp:6.2.0")
    implementation("org.kodein.emoji:emoji-kt:2.0.1")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(19)
}