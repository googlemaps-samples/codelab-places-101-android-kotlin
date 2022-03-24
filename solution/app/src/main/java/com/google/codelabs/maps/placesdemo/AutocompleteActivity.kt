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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.ktx.widget.PlaceSelectionError
import com.google.android.libraries.places.ktx.widget.PlaceSelectionSuccess
import com.google.android.libraries.places.ktx.widget.placeSelectionEvents
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class AutocompleteActivity : AppCompatActivity() {
    private lateinit var responseView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_autocomplete)

        // Set up view objects
        responseView = findViewById(R.id.autocomplete_response_content)
        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(listOf(Place.Field.NAME, Place.Field.ID, Place.Field.LAT_LNG, Place.Field.ADDRESS))

        // Listen to place selection events
        lifecycleScope.launchWhenCreated {
            autocompleteFragment.placeSelectionEvents().collect { event ->
                when (event) {
                    is PlaceSelectionSuccess -> {
                        val place = event.place
                        responseView.text = StringUtil.stringifyAutocompleteWidget(place, false)
                    }
                    is PlaceSelectionError -> Toast.makeText(
                        this@AutocompleteActivity,
                        "Failed to get place '${event.status.statusMessage}'",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
