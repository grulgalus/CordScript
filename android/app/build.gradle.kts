plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.cordbot.maker"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.cordbot.maker"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}
dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    // Java Discord API (JDA) pro nativního bota
    implementation("net.dv8tion:JDA:5.0.0-beta.21") {
        exclude(module = "opus-java")
    }
}
