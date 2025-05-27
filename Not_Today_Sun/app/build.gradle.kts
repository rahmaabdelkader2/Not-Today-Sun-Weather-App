plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id("kotlin-parcelize")
}

android {

    namespace = "com.example.not_today_sun"
    compileSdk = 35

    buildFeatures {
        viewBinding=true
        buildConfig=true
    }

//    testOptions {
//        unitTests {
//            all {
//                it.enabled = true
//                // Enable JUnit 5 platform
//                it.useJUnitPlatform()
//                // Enable detailed test logging
//            }
//        }
//    }

    defaultConfig {
        applicationId = "com.example.not_today_sun"
        minSdk = 34
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
//
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.room.common.jvm)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.junit.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation ("com.squareup.retrofit2:retrofit:2.11.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.11.0")

    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation ("androidx.work:work-runtime-ktx:2.10.1")

    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")

    implementation ("androidx.room:room-runtime:2.7.1")
    kapt ("androidx.room:room-compiler:2.7.1")
    implementation ("androidx.room:room-ktx:2.7.1")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.9.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation ("com.google.android.material:material:1.9.0")
    implementation ("androidx.recyclerview:recyclerview:1.3.0")
    implementation ("com.github.bumptech.glide:glide:4.12.0")

    implementation("com.google.android.gms:play-services-location:21.1.0")
    implementation ("com.airbnb.android:lottie:6.4.1")


    implementation ("androidx.activity:activity-ktx:1.9.3")
    implementation ("androidx.fragment:fragment-ktx:1.8.5")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
    implementation ("org.osmdroid:osmdroid-android:6.1.18")

    // Dependencies for local unit tests

    testImplementation ("androidx.test.ext:junit:1.1.3")
    testImplementation ("androidx.test.espresso:espresso-core:3.4.0")


    testImplementation ("org.hamcrest:hamcrest-library:2.2")
//
//    // JUnit 5 for org.junit.jupiter.api.Assertions
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
//
    // Kotlin Reflection for kotlin.reflect.full.* and kotlin.reflect.jvm.*
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.22")

    // AndroidX Test Runner and Rules
    testImplementation("androidx.test:runner:1.6.2")
    testImplementation("androidx.test:core:1.6.1")
    testImplementation("androidx.test.ext:junit:1.2.1")
    // JUnit for testing
//    testImplementation("junit:junit:4.13.2")
    // Mockk for mocking
    testImplementation("io.mockk:mockk-android:1.13.12")
    // Coroutines test support
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    // AndroidX core testing for LiveData/ViewModel
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    // Hamcrest for assertions
    testImplementation("org.hamcrest:hamcrest:2.2")


}