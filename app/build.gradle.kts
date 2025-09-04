import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.pardhu.smssyncer"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.pardhu.smssyncer"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Signing configuration for release builds (optional)
    signingConfigs {
        create("release") {
            val signingPropertiesFile = file("signing.properties")
            if (signingPropertiesFile.exists()) {
                val properties = Properties()
                properties.load(signingPropertiesFile.inputStream())

                val storeFile = properties["storeFile"] as String?
                val storePassword = properties["storePassword"] as String?
                val keyAlias = properties["keyAlias"] as String?
                val keyPassword = properties["keyPassword"] as String?

                if (storeFile != null &&
                                storePassword != null &&
                                keyAlias != null &&
                                keyPassword != null
                ) {
                    this.storeFile = file(storeFile)
                    this.storePassword = storePassword
                    this.keyAlias = keyAlias
                    this.keyPassword = keyPassword
                }
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
            )
            // Enable security features
            manifestPlaceholders["usesCleartextTraffic"] = "false"
            // Apply signing config if available
            signingConfig =
                    if (signingConfigs.findByName("release")?.storeFile?.exists() == true) {
                        signingConfigs.findByName("release")
                    } else {
                        null
                    }
        }
        debug {
            isMinifyEnabled = false
            manifestPlaceholders["usesCleartextTraffic"] = "true"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions { jvmTarget = "11" }

    buildFeatures { 
        compose = false
        viewBinding = true
    }

    // Enable security features
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    
    // Material Design components
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.ktx)

    // Security dependencies
    implementation(libs.androidx.security.crypto)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}