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

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.kotlin.awaitFetchPlace
import com.google.android.libraries.places.widget.PlaceAutocomplete
import com.google.android.libraries.places.widget.PlaceAutocompleteActivity
import kotlinx.coroutines.launch

class AutocompleteActivity : AppCompatActivity() {
    private lateinit var placesClient: PlacesClient
    private lateinit var responseView: TextView
    private lateinit var autocompleteButton: Button

    private val startAutocomplete =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val intent = result.data
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    if (intent != null) {
                        val prediction = PlaceAutocomplete.getPredictionFromIntent(intent)
                        val sessionToken = PlaceAutocomplete.getSessionTokenFromIntent(intent)
                        if (prediction != null) {
                            val placeFields = listOf(
                                Place.Field.DISPLAY_NAME,
                                Place.Field.ID,
                                Place.Field.LOCATION,
                                Place.Field.FORMATTED_ADDRESS
                            )
                            lifecycleScope.launch {
                                try {
                                    val response = placesClient.awaitFetchPlace(
                                        prediction.placeId,
                                        placeFields
                                    ) {
                                        this.sessionToken = sessionToken
                                    }
                                    responseView.text =
                                        prettyPrintAutocompleteWidget(response.place, false)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    responseView.text = e.message
                                }
                            }
                        }
                    }
                }
                PlaceAutocompleteActivity.RESULT_ERROR -> {
                    val status = intent?.let { PlaceAutocomplete.getResultStatusFromIntent(it) }
                    Toast.makeText(
                        this,
                        "Failed to get place '${status?.statusMessage}'",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_autocomplete)

        // Retrieve a PlacesClient (previously initialized - see DemoApplication)
        placesClient = Places.createClient(this)

        // Set up view objects
        responseView = findViewById(R.id.autocomplete_response_content)
        autocompleteButton = findViewById(R.id.autocomplete_button)

        // Launch Place Autocomplete when the button is clicked
        autocompleteButton.setOnClickListener {
            val intent = PlaceAutocomplete.createIntent(this)
            startAutocomplete.launch(intent)
        }
    }
}
