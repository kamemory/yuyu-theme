plugins {
    kotlin("jvm") version "2.0.21"
    `kotlin-dsl` version "5.1.2"
}

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
}