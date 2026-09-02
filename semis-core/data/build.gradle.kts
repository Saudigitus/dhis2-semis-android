import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.library")
    id("com.google.devtools.ksp")
    id("kotlin-parcelize")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.androidx.room)
}

apply(from = "${project.rootDir}/jacoco/jacoco.gradle.kts")
repositories {
    maven { url = uri("https://central.sonatype.com/repository/maven-snapshots") }
}

base {
    archivesName.set("psm-v" + libs.versions.vName.get())
}

android {
    namespace = "org.saudigitus.campaign.core.data"
    compileSdk = libs.versions.sdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    room {
        schemaDirectory("$projectDir/schemas")
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
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }

    // The enrollment form uses an isolated adapter source set. The original
    // Campaign sources are retained in the repository but depend on Campaign-
    // only modules that are not part of SEMIS.
    sourceSets {
        // Only the enrollment slice is compiled, as before. Up to AGP 8 the Kotlin source set
        // followed the Java one, so naming the slice here was enough to leave the rest of
        // src/main/java out of the build. AGP 9 keeps src/main/java in the Kotlin source set
        // regardless, so the slice has to be named for both languages or the two mutually
        // exclusive copies of campaignDataModule collide.
        getByName("main").java.setSrcDirs(listOf("src/semisEnrollment/java"))
        getByName("main").kotlin.setSrcDirs(listOf("src/semisEnrollment/java"))
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
    implementation(project(":commonskmm"))
    implementation(project(":tracker"))
    implementation(project(":dhis2-mobile-program-rules"))
    implementation(project(":semis-core:utils"))

    implementation(libs.androidx.coreKtx)
    implementation(libs.androidx.appcompat)
    implementation(libs.google.material)
    implementation(libs.kotlin.serialization.json)
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)

    // Ktor

    ksp(libs.room.compiler)

    coreLibraryDesugaring(libs.desugar)

    testImplementation(libs.test.junit)
    androidTestImplementation(libs.test.junit.ext)
    androidTestImplementation(libs.test.espresso)
}
