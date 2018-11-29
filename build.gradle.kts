import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.0"
}

group = "com.gt22"
version = "2.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    maven { url = uri("https://dl.bintray.com/kotlin/ktor") }
}
val jdaVersion = "3.8.1_439"
val gsonVersion = "2.8.5"
val ktorVersion = "1.0.0-beta-3"
dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.0.1")
    compile("net.dv8tion:JDA:$jdaVersion")
    compile("com.google.code.gson:gson:$gsonVersion")
    compile("org.slf4j:slf4j-simple:1.7.25")
    compile("de.codecentric.centerdevice:javafxsvg:1.3.0")
    compile("io.ktor:ktor-client-core:$ktorVersion")
    compile("io.ktor:ktor-client-jetty:$ktorVersion")
    compile("org.jetbrains.exposed:exposed:0.10.4")
    testCompile("junit:junit:4.12")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}