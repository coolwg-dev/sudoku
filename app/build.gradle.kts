plugins {
    id("com.android.application") version "8.2.2"
}

android {
    namespace = "com.LKCC.sudoku"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.LKCC.sudoku"
        minSdk = 21
        targetSdk = 35
        versionCode = 7
        versionName = "1.0.6"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildToolsVersion = "35.0.0"
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
}
