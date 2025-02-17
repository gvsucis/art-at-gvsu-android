plugins {
    id("com.android.library")
    id("kotlin-android")
    id("app.cash.sqldelight") version libs.versions.sqldelight
    id("org.jetbrains.kotlin.plugin.parcelize")
}

android {
    namespace = "edu.gvsu.art.client"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    sqldelight {
        databases {
            create("ArtGalleryDatabase") {
                val sqldelightVersion = libs.versions.sqldelight.get()

                packageName.set("edu.gvsu.art.db")
                verifyMigrations.set(true)
                dialect("app.cash.sqldelight:sqlite-3-38-dialect:$sqldelightVersion")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.12.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    testImplementation(kotlin("test"))
    testImplementation(libs.junit.junit)
    testImplementation(libs.sqldelight.sqlite.driver)
    testImplementation("io.mockk:mockk:1.13.12")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
}
