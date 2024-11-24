plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.test8"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.test8"
        minSdk = 28
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
    implementation(libs.skeletonlayout)
    implementation(libs.play.services.location)
    implementation(libs.okhttp)
    implementation(libs.dotsindicator)
    implementation(libs.legacy.support.v4)
    implementation(libs.maps.map.sdk)
    implementation(libs.fancytoast)
    implementation(libs.androidx.swiperefreshlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}