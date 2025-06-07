plugins {
    kotlin("jvm") version "2.0.0"
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.13.1"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.13.1")
}

tasks.test {
    useJUnitPlatform()
}