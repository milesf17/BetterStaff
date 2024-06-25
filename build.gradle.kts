plugins {
    id("java")
}

group = "xyz.lunarismc.betterstaff"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")

    compileOnly("net.luckperms:api:5.4")
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}