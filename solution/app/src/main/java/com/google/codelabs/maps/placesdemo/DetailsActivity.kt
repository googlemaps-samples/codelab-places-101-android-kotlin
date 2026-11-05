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

package com.google.codelabs.maps.placesdemo

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.kotlin.awaitFetchPlace
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

// [START places_101_kotlin_details_activity_skeleton]
class DetailsActivity : AppCompatActivity() {
    private lateinit var placesClient: PlacesClient
    private lateinit var detailsButton: Button
    private lateinit var detailsInput: TextInputEditText
    private lateinit var responseView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        // Set up view objects
        detailsInput = findViewById(R.id.details_input)
        detailsButton = findViewById(R.id.details_button)
        responseView = findViewById(R.id.details_response_content)

        val apiKey = BuildConfig.PLACES_API_KEY

        // Log an error if apiKey is not set.
        if (apiKey.isEmpty() || apiKey == "DEFAULT_API_KEY") {
            Log.e(TAG, "No api key")
            finish()
            return
        }

        // [START_EXCLUDE]
        // [START places_101_kotlin_details_create_client]
        // Retrieve a PlacesClient (previously initialized - see DemoApplication)
        placesClient = Places.createClient(this)
        // [END places_101_kotlin_details_create_client]

        // [START places_101_kotlin_details_click_listener]
        // Upon button click, fetch and display the Place Details
        detailsButton.setOnClickListener { button ->
            button.isEnabled = false
            val placeId = detailsInput.text.toString()
            val placeFields = listOf(
                Place.Field.DISPLAY_NAME,
                Place.Field.ID,
                Place.Field.LOCATION,
                Place.Field.FORMATTED_ADDRESS
            )
            lifecycleScope.launch {
                try {
                    val response = placesClient.awaitFetchPlace(placeId, placeFields)
                    responseView.text = response.prettyPrint()
                } catch (e: Exception) {
                    e.printStackTrace()
                    responseView.text = e.message
                }
                button.isEnabled = true
            }
        }
        // [END places_101_kotlin_details_click_listener]
        // [END_EXCLUDE]
    }

    companion object {
        private const val TAG = "DetailsActivity"
    }
}
// [END places_101_kotlin_details_activity_skeleton]
