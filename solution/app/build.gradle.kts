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

// [START places_101_kotlin_secrets_plugin]
plugins {
    alias(libs.plugins.android.application)
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}
// [END places_101_kotlin_secrets_plugin]

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

    // [START places_101_kotlin_maps_dependencies]
    // [START places_101_kotlin_dependencies]
    // Provides the Places SDK for Android, including Place Details, Autocomplete, Nearby Search, and built-in KTX coroutine extensions
    implementation(libs.places)
    // [START_EXCLUDE]
    // Provides the Maps SDK for Android (SupportMapFragment, GoogleMap, LatLng, and MarkerOptions) to display places on a map
    implementation(libs.play.services.maps)
    // [END_EXCLUDE]
    // Provides FusedLocationProviderClient to retrieve the device's current location for nearby place searches
    implementation(libs.play.services.location)
    // Provides the Task.await() coroutine extension to suspend on Google Play services location Tasks
    implementation(libs.kotlinx.coroutines.play.services)
    // [END places_101_kotlin_dependencies]
    // [END places_101_kotlin_maps_dependencies]
}

// [START places_101_kotlin_secrets_config]
secrets {
    // Optionally specify a different filename containing your secrets.
    // The plugin defaults to "local.properties"
    propertiesFileName = "secrets.properties"

    // A properties file containing default secret values. This file can be
    // checked in version control.
    defaultPropertiesFileName = "local.defaults.properties"
}
// [END places_101_kotlin_secrets_config]
