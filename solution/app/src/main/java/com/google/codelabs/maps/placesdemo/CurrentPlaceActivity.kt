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
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceLikelihood
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
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
            checkPermissionThenFindCurrentPlace()
        }

    }

    /**
     * Checks that the user has granted permission for fine or coarse location.
     * If granted, finds current Place.
     * If not yet granted, launches the permission request.
     * See https://developer.android.com/training/permissions/requesting
     */
    private fun checkPermissionThenFindCurrentPlace() {
        when {
            (ContextCompat.checkSelfPermission(
                this,
                ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) -> {
                // You can use the API that requires the permission.
                findCurrentPlace()
            }
            shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)
            -> {
                Log.d(TAG, "Showing permission rationale dialog")
                // TODO: In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected. In this UI,
                // include a "cancel" or "no thanks" button that allows the user to
                // continue using your app without granting the permission.
            }
            else -> {
                // Ask for both the ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION permissions.
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode != PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
            )
            return
        } else if (permissions.toList().zip(grantResults.toList())
                .firstOrNull { (permission, grantResult) ->
                    grantResult == PackageManager.PERMISSION_GRANTED && (permission == ACCESS_FINE_LOCATION || permission == ACCESS_COARSE_LOCATION)
                } != null
        )
            // At least one location permission has been granted, so proceed with Find Current Place
            findCurrentPlace()
    }

    /**
     * Fetches a list of [PlaceLikelihood] instances that represent the Places the user is
     * most
     * likely to be at currently.
     */
    @RequiresPermission(anyOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
    private fun findCurrentPlace() {
        // Use fields to define the data types to return.
        val placeFields: List<Place.Field> =
            listOf(Place.Field.NAME, Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG)

        // Use the builder to create a FindCurrentPlaceRequest.
        val request: FindCurrentPlaceRequest = FindCurrentPlaceRequest.newInstance(placeFields)

        // Call findCurrentPlace and handle the response (first check that the user has granted permission).
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        ) {
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
        } else {
            Log.d(TAG, "LOCATION permission not granted")
            checkPermissionThenFindCurrentPlace()

        }
    }

    companion object {
        private val TAG = "CurrentPlaceActivity"
        private const val PERMISSION_REQUEST_CODE = 9
    }
}

fun FindCurrentPlaceResponse.prettyPrint(): String {
    return StringUtil.stringify(this, false)
}