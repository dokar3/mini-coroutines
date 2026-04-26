plugins {
    kotlin("jvm") version "2.0.0"
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.14.4"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.14.4")
}

tasks.test {
    useJUnitPlatform()
}