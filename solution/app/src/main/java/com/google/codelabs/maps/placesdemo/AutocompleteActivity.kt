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
import com.google.android.libraries.places.widget.BasicPlaceAutocomplete
import com.google.android.libraries.places.widget.PlaceAutocompleteActivity

class AutocompleteActivity : AppCompatActivity() {
    private lateinit var responseView: TextView
    private lateinit var autocompleteButton: Button

    private val startAutocomplete =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val intent = result.data
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    val place = intent?.let { BasicPlaceAutocomplete.getPlaceFromIntent(it) }
                    if (place != null) {
                        responseView.text = prettyPrintAutocompleteWidget(place, false)
                    }
                }
                PlaceAutocompleteActivity.RESULT_ERROR -> {
                    val status = intent?.let { BasicPlaceAutocomplete.getResultStatusFromIntent(it) }
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

        // Set up view objects
        responseView = findViewById(R.id.autocomplete_response_content)
        autocompleteButton = findViewById(R.id.autocomplete_button)

        // Launch Place Autocomplete when the button is clicked
        autocompleteButton.setOnClickListener {
            val intent = BasicPlaceAutocomplete.createIntent(this)
            startAutocomplete.launch(intent)
        }
    }
}
