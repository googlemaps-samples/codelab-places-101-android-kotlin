/*
 * Copyright 2022 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.google.codelabs.maps.placesdemo"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.google.codelabs.maps.placesdemo"
        minSdk = 24
        targetSdk = 37
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        buildConfig = true
    }
    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    // Provides idiomatic Kotlin extensions for core Android framework APIs
    implementation(libs.androidx.core.ktx)
    // Provides backward-compatible AppCompatActivity, ActionBar, and AlertDialog themes
    implementation(libs.androidx.appcompat)
    // Provides the modern Activity Result API (registerForActivityResult) for Place Autocomplete
    implementation(libs.androidx.activity.ktx)
    // Provides Material Design UI components such as TextInputLayout and TextInputEditText
    implementation(libs.material)
    // Provides flexible ConstraintLayout positioning for activity views
    implementation(libs.androidx.constraintlayout)
    // Provides lifecycle-aware coroutine scopes (lifecycleScope) for asynchronous SDK requests
    implementation(libs.androidx.lifecycle.runtime.ktx)
    // Provides the JUnit 4 framework for local unit tests
    testImplementation(libs.junit)
    // Provides AndroidX JUnit extensions for instrumented tests
    androidTestImplementation(libs.androidx.test.ext.junit)
    // Provides the Espresso UI testing framework for instrumented Android tests
    androidTestImplementation(libs.androidx.espresso.core)

    // Pre-existing utility file StringUtil.kt is dependent on the Places SDK library
    compileOnly(libs.places)
    // Pre-existing utility file StringUtil.kt is dependent on the Maps SDK library (LatLng)
    compileOnly(libs.play.services.maps)
}
