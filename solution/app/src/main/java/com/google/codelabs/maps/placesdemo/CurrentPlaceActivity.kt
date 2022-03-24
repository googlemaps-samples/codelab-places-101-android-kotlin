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

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.ktx.api.net.awaitFindCurrentPlace
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class CurrentPlaceActivity : AppCompatActivity() {
    private lateinit var placesClient: PlacesClient
    private lateinit var currentButton: Button
    private lateinit var responseView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_current)

        // Retrieve a PlacesClient (previously initialized - see DemoApplication)
        placesClient = Places.createClient(this)

        // Set view objects
        currentButton = findViewById(R.id.current_button)
        responseView = findViewById(R.id.current_response_content)


        // Set listener for initiating Current Place
        currentButton.setOnClickListener {
            findCurrentPlace()
        }
    }

    /**
     * Fetches a list of [PlaceLikelihood] instances that represent the Places the user is
     * most
     * likely to be at currently.
     */
    private fun findCurrentPlace() {
        // If fine location access has not yet been granted to this app, request the permission.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0)
            return
        }
        findCurrentPlaceWithPermissions()
    }

    /**
     * Fetches a list of [PlaceLikelihood] instances that represent the Places the user is
     * most likely to be at currently.
     */
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_WIFI_STATE])
    private fun findCurrentPlaceWithPermissions() {
        // Use fields to define the data types to return.
        val placeFields: List<Place.Field> = listOf(Place.Field.NAME, Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG)

        // Retrieve likely places based on the device's current location
        lifecycleScope.launch {
            try {
                val response = placesClient.awaitFindCurrentPlace(placeFields)
                responseView.text = response.prettyPrint()

                // Enable scrolling on the long list of likely places
                val movementMethod = ScrollingMovementMethod()
                responseView.movementMethod = movementMethod
            } catch (e: Exception) {
                e.printStackTrace()
                responseView.text = e.message
            }
        }
    }
}

fun FindCurrentPlaceResponse.prettyPrint(): String {
    return StringUtil.stringify(this, false)
}