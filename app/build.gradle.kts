import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().takeIf { keystorePropertiesFile.exists() }?.apply {
    load(FileInputStream(keystorePropertiesFile))
}

android {
    namespace = "com.amaterasu.expense_tracker"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.amaterasu.expense_tracker"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            abiFilters += listOf("arm64-v8a")
        }
    }

    signingConfigs {
        if (keystoreProperties != null) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            buildConfigField("boolean", "TEST_REIMPORT_ON_LAUNCH", "true")
        }
        release {
            buildConfigField("boolean", "TEST_REIMPORT_ON_LAUNCH", "false")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (keystoreProperties != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":shared"))

    // ---------- Core ----------
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // ---------- Compose ----------
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.transport.runtime)
    implementation(libs.androidx.monitor)
    implementation(libs.androidx.junit.ktx)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // ---------- Activity + ViewModel ----------
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)


    // ---------- Room ----------
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // ---------- WorkManager ----------
    implementation(libs.androidx.work.runtime.ktx)

    // ---------- Tests ----------
//    testImplementation("junit:junit:4.13.2")
//    testImplementation(libs.junit4)
//    androidTestImplementation(libs.androidx.junit)
//    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    testImplementation ("junit:junit:4.13.2")
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
