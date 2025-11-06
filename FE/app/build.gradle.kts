plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.project"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.project"
        minSdk = 24
        targetSdk = 36
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.viewpager2)
    implementation(libs.swiperefreshlayout)
    
    // Network dependencies
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Socket.IO client for real-time chat
    implementation("io.socket:socket.io-client:2.1.0")
    
    // Image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    
    // OpenStreetMap - Miễn phí hoàn toàn, không cần API key
    implementation("org.osmdroid:osmdroid-android:6.1.17")
    implementation("com.github.MKergall:osmbonuspack:6.9.0")
    
    // Google Play Services Location (for GPS)
    implementation("com.google.android.gms:play-services-location:21.0.1")
    
    // Notification support
    implementation("androidx.core:core:1.12.0")
    
    // ShortcutBadger for app icon badge support (Samsung, Xiaomi, etc.)
    implementation("me.leolin:ShortcutBadger:1.1.22")
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}