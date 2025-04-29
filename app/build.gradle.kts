plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.rentals_g03"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.rentals_g03"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // --- Define API Key Holder ---
    // This makes the API key accessible in your Kotlin code via BuildConfig
    // Replace "YOUR_API_KEY_HERE" with the key you got from Google Cloud Console!
    // Note: For real apps, use more secure methods like Secrets Gradle Plugin.
    //buildConfigField("String", "PLACES_API_KEY", "\"AIzaSyDpr2x4vRfdJgeKO1bxpbbXQ5zhAkRgAzA\"")

        // my google api key waiting for it to activate, took some time
    buildConfigField("String", "PLACES_API_KEY", "\"AIzaSyDXTKLacb1QqaHPy_n4HBV_i5Jkb9F2Sw4\"")
        // --- End API Key Holder ---
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
        buildConfig = true // Enable BuildConfig generation for API Key
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Image glide online
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // google maps
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    // firebase-firestore
    implementation(platform("com.google.firebase:firebase-bom:33.12.0"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")

    // Google Places SDK for Android
    implementation("com.google.android.libraries.places:places:3.4.0")
}