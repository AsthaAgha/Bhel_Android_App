plugins {
    alias(libs.plugins.androidApplication)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.bhelvisualizer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.bhelvisualizer"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.database)
    implementation(libs.swiperefreshlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    implementation(libs.mpandroidchart)
    androidTestImplementation(libs.espresso.core)
    //noinspection UseTomlInstead
    implementation("com.google.firebase:firebase-auth:23.0.0")
    //noinspection UseTomlInstead
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    //noinspection UseTomlInstead
    implementation("com.google.firebase:firebase-analytics")
}
