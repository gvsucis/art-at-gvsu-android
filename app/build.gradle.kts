import java.util.Properties

plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("kotlin-android")
    id("kotlin-kapt")
    id("com.google.firebase.crashlytics")
    id("org.jetbrains.kotlin.plugin.parcelize")
    kotlin("plugin.serialization") version libs.versions.kotlin
    alias(libs.plugins.compose.compiler)
}

val properties = Properties().apply {
    val file = rootProject.file("project.properties")
    if (file.exists()) {
        load(file.inputStream())
    } else {
        load(rootProject.file("project-sample.properties").inputStream())
    }
}

val secrets = Properties().apply {
    val file = rootProject.file("secrets.properties")
    if (file.exists()) {
        load(file.inputStream())
    }
}

android {
    namespace = "edu.gvsu.art.gallery"
    compileSdk = 35

    defaultConfig {
        applicationId = "edu.gvsu.artmuseum"
        minSdk = 31
        targetSdk = 35
        versionCode = 1024
        versionName = "2025.07.1024"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "ART_GALLERY_BASE_URL",
            properties["art_gallery_base_url"] as String
        )
        buildConfigField(
            "String",
            "VISION_SEARCH_BASE_URL",
            properties["vision_search_base_url"] as String
        )
        buildConfigField("String", "APPLICATION_NAME", properties["application_name"] as String)
    }

    buildFeatures {
        compose = true
    }

    signingConfigs {
        create("release") {
            storeFile = file("${project.rootDir}/release.keystore")
            storePassword = secrets["store_password"] as String
            keyAlias = secrets["key_alias"] as String
            keyPassword = secrets["store_password"] as String
        }
        getByName("debug") {
            storeFile = file("${project.rootDir}/debug.keystore")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "art-gvsu-proguard.txt")
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            isDebuggable = true
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
}

dependencies {
    val camerax_version = "1.3.4"
    val datastore_version = "1.1.1"
    val accompanist_version = "0.22.0-rc"

    implementation(libs.androidx.compose.bom)
    implementation("androidx.camera:camera-core:${camerax_version}")
    implementation("androidx.camera:camera-camera2:${camerax_version}")
    implementation("androidx.camera:camera-lifecycle:${camerax_version}")
    implementation("androidx.camera:camera-video:${camerax_version}")
    implementation("androidx.camera:camera-view:${camerax_version}")
    implementation("androidx.camera:camera-extensions:${camerax_version}")
    implementation("androidx.camera:camera-view:${camerax_version}")
    implementation("androidx.camera:camera-lifecycle:${camerax_version}")
    implementation("androidx.media3:media3-ui:1.4.1")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.1")
    implementation("androidx.datastore:datastore-preferences:${datastore_version}")
    implementation(libs.androidx.animation)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material)
    implementation("com.google.android.material:material:1.12.0")
    implementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.ui.util)
    implementation(libs.kotlinx.serialization.json)
    implementation("androidx.datastore:datastore-preferences:${datastore_version}")
    implementation(libs.androidx.navigation.compose)
    implementation("com.google.mlkit:barcode-scanning:17.3.0")
    implementation("com.github.Tlaster:Swiper:0.7.1")
    implementation("com.google.accompanist:accompanist-permissions:${accompanist_version}")
    implementation("androidx.media3:media3-exoplayer:1.4.1")
    implementation("androidx.media3:media3-datasource-okhttp:1.4.1")
    implementation(libs.sqldelight.android.driver)
    implementation(libs.coil.compose)
    implementation(libs.coil.video)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.core)
    implementation(libs.koin.androidx.workmanager)
    implementation(libs.jsoup)
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("com.google.android.gms:play-services-analytics:18.1.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation(platform("com.google.firebase:firebase-bom:33.2.0"))
    implementation("com.gorisse.thomas.sceneform:sceneform:1.23.0")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.maps.android:android-maps-utils:2.2.3")
    implementation("com.google.maps.android:maps-ktx:3.2.1")
    implementation("com.google.maps.android:maps-utils-ktx:3.2.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.android.filament:filamat-android:1.21.1")
    implementation(project(":artgalleryclient"))

    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.sqldelight.sqlite.driver)
    testImplementation(kotlin("test"))
    testImplementation(libs.junit.junit)
    androidTestImplementation(libs.junit.junit)
}

tasks.register("useGoogleServicesDebugFile") {
    description = "Copies the debug google-services.json file if file is missing."
    doLast {
        val googleServicesFile = "google-services.json"
        if (!file("${project.projectDir}/$googleServicesFile").exists()) {
            val debugOnlyFile = "google-services-debug-only.json"
            println("$googleServicesFile file is missing. Copying $debugOnlyFile")
            copy {
                from("${project.projectDir}/$debugOnlyFile")
                into(project.projectDir)
                rename { googleServicesFile }
            }
        }
    }
}

tasks.named("preBuild") {
    dependsOn("useGoogleServicesDebugFile")
}
