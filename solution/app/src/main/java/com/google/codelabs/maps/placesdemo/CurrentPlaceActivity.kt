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

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.BundleCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.kotlin.awaitSearchNearby
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// [START places_101_kotlin_current_place_full]
// [START places_101_kotlin_place_item_ui_model]
/**
 * UI model representing a candidate place item in the selection dialog and on the map.
 *
 * Abstracting the Places SDK network model ([Place]) from the UI state decouples
 * the presentation layer from network-specific types and makes the mapping logic
 * directly unit-testable on the JVM.
 */
internal data class PlaceItemUiModel(
    val title: String,
    val snippet: String,
    val position: LatLng
)

/**
 * Maps a network [Place] returned by the Places SDK to a [PlaceItemUiModel] for UI rendering,
 * or returns `null` if the place has no geographic [Place.getLocation].
 */
internal fun toPlaceItemUiModel(place: Place, defaultTitle: String): PlaceItemUiModel? {
    val position = place.location ?: return null
    val title = place.displayName ?: defaultTitle
    val snippet = buildString {
        place.formattedAddress?.let { append(it) }
        place.attributions?.takeIf { it.isNotEmpty() }?.let {
            if (isNotEmpty()) append("\n")
            append(it.joinToString("\n"))
        }
    }
    return PlaceItemUiModel(title = title, snippet = snippet, position = position)
}
// [END places_101_kotlin_place_item_ui_model]

// [START places_101_kotlin_current_place_on_create]
class CurrentPlaceActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var placesClient: PlacesClient
    private lateinit var currentButton: Button
    private lateinit var responseView: TextView
    private var map: GoogleMap? = null
    private val defaultLocation = LatLng(-33.8523341, 151.2106085)

    // [START_EXCLUDE]
    private val likelyPlaces = mutableListOf<PlaceItemUiModel>()

    private var lastKnownLocation: LatLng? = null
    // [END_EXCLUDE]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_current)

        // [START_EXCLUDE]
        if (savedInstanceState != null) {
            lastKnownLocation = BundleCompat.getParcelable(
                savedInstanceState,
                KEY_LOCATION,
                LatLng::class.java
            )
        }
        // [END_EXCLUDE]

        val apiKey = BuildConfig.PLACES_API_KEY

        // Log an error if apiKey is not set.
        if (apiKey.isEmpty() || apiKey == "DEFAULT_API_KEY") {
            Log.e(TAG, "No api key")
            finish()
            return
        }

        // Retrieve a PlacesClient (previously initialized - see DemoApplication)
        placesClient = Places.createClient(this)
        // [START_EXCLUDE]
        (supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?)?.getMapAsync(this)
        // [END_EXCLUDE]

        // Set view objects
        currentButton = findViewById(R.id.current_button)
        responseView = findViewById(R.id.current_response_content)

        // Set listener for initiating Current Place
        currentButton.setOnClickListener {
            checkPermissionThenFindCurrentPlace()
        }
    }

    // [START_EXCLUDE]
    /**
     * Saves the state of the map when the activity is paused.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(KEY_LOCATION, lastKnownLocation)
        super.onSaveInstanceState(outState)
    }

    // [START places_101_kotlin_check_permission]
    /**
     * Checks that the user has granted permission for fine or coarse location.
     * If granted, searches for nearby places around the current location.
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
                searchNearbyPlaces()
            }

            shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)
            -> {
                Log.d(TAG, "Showing permission rationale dialog")
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected. In this UI,
                // include a "cancel" or "no thanks" button that allows the user to
                // continue using your app without granting the permission.
            }

            else -> {
                // Ask for both the ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION permissions.
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        ACCESS_FINE_LOCATION,
                        ACCESS_COARSE_LOCATION
                    ),
                    PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    // [START_EXCLUDE]
    // [START places_101_kotlin_on_request_permissions_result]
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
        } else if (
            permissions.toList().zip(grantResults.toList())
                .firstOrNull { (permission, grantResult) ->
                    grantResult == PackageManager.PERMISSION_GRANTED && (permission == ACCESS_FINE_LOCATION || permission == ACCESS_COARSE_LOCATION)
                } != null
        )
        // At least one location permission has been granted, so proceed with searching nearby places
        searchNearbyPlaces()
    }
    // [END places_101_kotlin_on_request_permissions_result]

    // [START places_101_kotlin_search_nearby_places]
    /**
     * Fetches a list of nearby [Place] instances around the device's current location.
     */
    @RequiresPermission(anyOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
    private fun searchNearbyPlaces() {
        // Use fields to define the data types to return.
        val placeFields: List<Place.Field> =
            listOf(
                Place.Field.DISPLAY_NAME,
                Place.Field.ID,
                Place.Field.FORMATTED_ADDRESS,
                Place.Field.LOCATION
            )

        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            // Retrieve nearby places based on the device's current location
            currentButton.isEnabled = false
            lifecycleScope.launch {
                try {
                    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@CurrentPlaceActivity)
                    val location = fusedLocationClient.lastLocation.await()
                    val searchCenter = location?.let { LatLng(it.latitude, it.longitude) } ?: defaultLocation
                    val circle = CircularBounds.newInstance(searchCenter, 500.0)
                    val response = placesClient.awaitSearchNearby(circle, placeFields)

                    // [START_EXCLUDE]
                    likelyPlaces.clear()

                    likelyPlaces.addAll(
                        response.places.take(M_MAX_ENTRIES).mapNotNull { place ->
                            place.toUiModel()
                        }
                    )

                    openPlacesDialog()
                    // [END_EXCLUDE]

                    responseView.text = response.prettyPrint()

                    // Enable scrolling on the long list of likely places
                    val movementMethod = ScrollingMovementMethod()
                    responseView.movementMethod = movementMethod
                } catch (e: Exception) {
                    e.printStackTrace()
                    responseView.text = e.message
                    currentButton.isEnabled = true
                }
            }
        } else {
            Log.d(TAG, "LOCATION permission not granted")
            checkPermissionThenFindCurrentPlace()
        }
    }
    // [END places_101_kotlin_search_nearby_places]

    // [START places_101_kotlin_open_places_dialog]
    /**
     * Displays a form allowing the user to select a place from a list of likely places.
     */
    private fun openPlacesDialog() {
        // Ask the user to choose the place where they are now.
        val listener =
            DialogInterface.OnClickListener { _, which -> // The "which" argument contains the position of the selected item.
                val selectedPlace = likelyPlaces[which]
                lastKnownLocation = selectedPlace.position

                map?.clear()

                setPlaceOnMap(selectedPlace)
            }

        // Display the dialog.
        AlertDialog.Builder(this)
            .setTitle(R.string.pick_place)
            .setItems(likelyPlaces.map { it.title }.toTypedArray(), listener)
            .setOnDismissListener {
                currentButton.isEnabled = true
            }
            .show()
    }
    // [END places_101_kotlin_open_places_dialog]

    // [START places_101_kotlin_on_map_ready]
    override fun onMapReady(map: GoogleMap) {
        this.map = map
        lastKnownLocation?.let { location ->
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    location,
                    DEFAULT_ZOOM
                )
            )
        }
    }

    private fun setPlaceOnMap(place: PlaceItemUiModel) {
        map?.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                place.position,
                DEFAULT_ZOOM
            )
        )
        map?.addMarker(
            MarkerOptions()
                .position(place.position)
                .title(place.title)
                .snippet(place.snippet)
        )
    }

    private fun Place.toUiModel(): PlaceItemUiModel? =
        toPlaceItemUiModel(this, getString(R.string.default_info_title))
    // [END places_101_kotlin_on_map_ready]
    // [END_EXCLUDE]

    companion object {
        private const val TAG = "CurrentPlaceActivity"
        private const val PERMISSION_REQUEST_CODE = 9
        // [START_EXCLUDE]
        private const val DEFAULT_ZOOM = 15f

        // Key for storing activity state.
        private const val KEY_LOCATION = "location"

        private const val M_MAX_ENTRIES = 5
        // [END_EXCLUDE]
    }
    // [END places_101_kotlin_check_permission]
    // [END_EXCLUDE]
}
// [END places_101_kotlin_current_place_on_create]
// [END places_101_kotlin_current_place_full]
