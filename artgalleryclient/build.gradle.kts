plugins {
    id("com.android.library")
    id("kotlin-android")
    id("com.squareup.sqldelight")
    id("org.jetbrains.kotlin.plugin.parcelize")
}

android {
    namespace = "edu.gvsu.art.client"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
    }
}

sqldelight {
    database("ArtGalleryDatabase") {
        packageName = "edu.gvsu.art.db"
        verifyMigrations = true
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.12.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    testImplementation(kotlin("test"))
    testImplementation("junit:junit:4.13.2")
    testImplementation("com.squareup.sqldelight:sqlite-driver:1.5.3")
    testImplementation("io.mockk:mockk:1.13.12")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
