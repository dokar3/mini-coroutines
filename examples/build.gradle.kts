plugins {
    kotlin("jvm") version "1.9.21"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":coroutines"))
}

kotlin {
    jvmToolchain(21)
}
