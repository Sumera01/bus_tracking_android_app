plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.bustrackingapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.bustrackingapp"
        minSdk = 24
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // ✅ AndroidX & Material Design
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.activity)

    // ✅ Google Maps SDK
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    // ✅ Google Location Services (for GPS, Geofencing)
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // ✅ Google Maps Utils (for decoding polylines)
    implementation("com.google.maps.android:android-maps-utils:2.3.0")

    // ✅ Retrofit (for Directions API)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // ✅ Gson (used for JSON parsing)
    implementation("com.google.code.gson:gson:2.10.1")

    // ✅ Coroutines (if used elsewhere)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // ✅ Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    
    //Notification
    implementation ("com.android.volley:volley:1.2.1")

}
