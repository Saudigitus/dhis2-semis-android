import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
    id("dagger.hilt.android.plugin")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose.compiler)
}

apply(from = "${project.rootDir}/jacoco/jacoco.gradle.kts")

repositories {
    maven { url = uri("https://central.sonatype.com/repository/maven-snapshots") }
}

base {
    archivesName.set("psm-v" + libs.versions.vName.get())
}

android {
    namespace = "org.saudigitus.semis.performance"
    compileSdk = libs.versions.sdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        viewBinding = true
        dataBinding = true
    }

    packaging {
        resources {
            excludes.addAll(
                mutableSetOf(
                    "META-INF/DEPENDENCIES",
                    "META-INF/ASL2.0",
                    "META-INF/NOTICE",
                    "META-INF/LICENSE",
                    "META-INF/proguard/androidx-annotations.pro",
                    "META-INF/gradle/incremental.annotation.processors"
                )
            )
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":commons"))
    implementation(project(":semis:core:data"))
    implementation(project(":semis:core:designsystem"))
    implementation(project(":semis:core:utils"))

    implementation(libs.androidx.coreKtx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.viewModelKtx)
    implementation(libs.navigationCompose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.compose.materialIcons)
    implementation(libs.androidx.compose.material.iconsExtended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.uitooling)
    implementation(libs.google.material)
    implementation(libs.dagger.hilt.android)

    kapt(libs.dagger.hilt.android.compiler)
    coreLibraryDesugaring(libs.desugar)

    testImplementation(libs.test.junit)
    androidTestImplementation(libs.test.junit.ext)
    androidTestImplementation(libs.test.espresso)
}

kapt {
    correctErrorTypes = true
}