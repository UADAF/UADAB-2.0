import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.71"
    id("com.github.johnrengelman.shadow") version "4.0.4"
}

group = "com.gt22"
version = "2.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    maven { url = uri("http://52.48.142.75/maven") }
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://dl.bintray.com/kotlin/ktor") }
}
val jdaVersion = "3.8.3_462"
val gsonVersion = "2.8.5"
val ktorVersion = "1.2.1"
dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.8")
    implementation("net.dv8tion:JDA:$jdaVersion")
    implementation("com.google.code.gson:gson:$gsonVersion")
    implementation("org.slf4j:slf4j-simple:1.7.25")
    implementation("de.codecentric.centerdevice:javafxsvg:1.3.0")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-apache:$ktorVersion")
    implementation("org.jetbrains.exposed:exposed:0.10.4")
    implementation("mysql:mysql-connector-java:6.0.6")
    implementation("com.github.kizitonwose.time:time:1.0.2")
    implementation("com.uadaf:uadamusic:2.5")
    implementation("com.uadaf:quoter-api:1.3.1")
    implementation("com.sedmelluq:lavaplayer:1.3.12")
    implementation("pl.droidsonroids:jspoon:1.3.2")
    testImplementation("junit:junit:4.12")
}

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Main-Class"] = "UADAB"
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
val compileKotlin: KotlinCompile by tasks

compileKotlin.kotlinOptions {
    languageVersion = "1.3"
}