plugins {
    val kotlinVersion = "1.7.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.13.4"
}

group = "top.simsoft"
version = "0.1.0"

repositories {
    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
}

dependencies{
    compileOnly("net.mamoe:mirai-core-jvm:2.13.4")
}

mirai{
    jvmTarget = org.gradle.api.JavaVersion.VERSION_17
}