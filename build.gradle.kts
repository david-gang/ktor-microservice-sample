plugins {
    id("application")
    id("org.jetbrains.kotlin.jvm").version("1.6.10")
    kotlin("plugin.serialization").version("1.6.10")
    id("com.github.johnrengelman.shadow").version("7.0.0")
}

group = "org.example"
version ="1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    val ktorVersion:String by project
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-cio:$ktorVersion")
    implementation("io.ktor:ktor-serialization:$ktorVersion")
    implementation("io.ktor:ktor-auth:$ktorVersion")
//    implementation("io.ktor:ktor-metrics-micrometer:$ktorVersion")
//    implementation("io.micrometer:micrometer-registry-prometheus:1.8.1")
    implementation("ch.qos.logback:logback-classic:1.2.5")

    val exposedVersion: String by project
        implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
        implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
        implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.postgresql:postgresql:42.3.1")
    implementation("com.zaxxer:HikariCP:5.0.1")

    // https://mvnrepository.com/artifact/io.lettuce/lettuce-core
    implementation("io.lettuce:lettuce-core:6.1.5.RELEASE")
    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-reactive
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.6.0")



    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.2.0")


}

application {
    mainClass.set("com.example.ApplicationKt")
}

tasks{
    shadowJar {
        manifest {
            attributes(Pair("Main-Class", "io.ktor.server.cio.EngineMain"))
        }
    }
}


