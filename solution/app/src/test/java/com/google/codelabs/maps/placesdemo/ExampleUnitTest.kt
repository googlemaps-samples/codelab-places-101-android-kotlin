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

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchNearbyResponse
import com.google.android.libraries.places.api.net.kotlin.awaitFetchPlace
import com.google.android.libraries.places.api.net.kotlin.awaitSearchNearby
import com.google.maps.android.robolectric.annotation.EnableMapsShadows
import com.google.maps.android.robolectric.ktx.cameraTarget
import com.google.maps.android.robolectric.ktx.cameraZoom
import com.google.maps.android.robolectric.ktx.createTestMap
import com.google.maps.android.robolectric.ktx.findMarker
import com.google.maps.android.robolectric.ktx.markers
import com.google.maps.android.robolectric.ktx.visibleBounds
import com.google.maps.android.robolectric.powerassert.assertMap
import com.google.maps.android.robolectric.powerassert.assertMarker
import com.google.maps.android.robolectric.powerassert.contains
import com.google.maps.android.robolectric.powerassert.distanceTo
import com.google.maps.android.robolectric.powerassert.meters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Headless JVM unit and Robolectric tests verifying StringUtil formatting,
 * PlaceItemUiModel mapping, Demo navigation, and CurrentPlaceActivity map behavior
 * using `android-maps-testing` (`:robolectric-testing`) and Kotlin Power-Assert.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
@EnableMapsShadows
class ExampleUnitTest {

    private val sydneyOperaHouse = LatLng(-33.8567844, 151.2152967)
    private val googleplex = LatLng(37.4220, -122.0841)

    private fun buildSamplePlace(): Place =
        Place.builder()
            .setDisplayName("Sydney Opera House")
            .setId("ChIJ3S-JXmauEmsRUcIaWtf4MzE")
            .setLocation(sydneyOperaHouse)
            .setFormattedAddress("Bennelong Point, Sydney NSW 2000, Australia")
            .build()

    @Test
    fun fetchPlaceResponse_prettyPrint_formatsExpectedOutput() {
        val place = buildSamplePlace()
        val response = FetchPlaceResponse.newInstance(place)

        val formatted = response.prettyPrint()
        val expected =
            "Fetch Place Result:$RESULT_SEPARATOR" +
                "Sydney Opera House (ChIJ3S-JXmauEmsRUcIaWtf4MzE) is located at " +
                "-33.8567844, 151.2152967 (Bennelong Point, Sydney NSW 2000, Australia)"
        check(formatted == expected)
    }

    @Test
    fun searchNearbyResponse_prettyPrint_emptyList() {
        val response = SearchNearbyResponse.newInstance(emptyList())
        check(response.prettyPrint() == "0 Current Place Results:")
    }

    @Test
    fun searchNearbyResponse_prettyPrint_populatedList() {
        val place = buildSamplePlace()
        val response = SearchNearbyResponse.newInstance(listOf(place))

        val formatted = response.prettyPrint()
        val expected =
            "1 Current Place Results:$RESULT_SEPARATOR" +
                "Sydney Opera House (ChIJ3S-JXmauEmsRUcIaWtf4MzE) is located at " +
                "-33.8567844, 151.2152967 (Bennelong Point, Sydney NSW 2000, Australia)"
        check(formatted == expected)
    }

    @Test
    fun prettyPrintAutocompleteWidget_formatsPrettyAndRaw() {
        val place = buildSamplePlace()

        val pretty = prettyPrintAutocompleteWidget(place, raw = false)
        val expectedPretty =
            "Autocomplete Widget Result:$RESULT_SEPARATOR" +
                "Sydney Opera House (ChIJ3S-JXmauEmsRUcIaWtf4MzE) is located at " +
                "-33.8567844, 151.2152967 (Bennelong Point, Sydney NSW 2000, Australia)"
        check(pretty == expectedPretty)

        val raw = prettyPrintAutocompleteWidget(place, raw = true)
        check(
            raw.startsWith("Autocomplete Widget Result:$RESULT_SEPARATOR") &&
                raw.contains("Sydney Opera House")
        )
    }

    @Test
    fun toPlaceItemUiModel_withAllFields_mapsExpectedUiModel() {
        val place = Place.builder()
            .setDisplayName("Googleplex")
            .setFormattedAddress("1600 Amphitheatre Pkwy")
            .setAttributions(listOf("Attribution 1", "Attribution 2"))
            .setLocation(googleplex)
            .build()

        val uiModel = toPlaceItemUiModel(place, "Default Title")

        check(
            uiModel == PlaceItemUiModel(
                title = "Googleplex",
                snippet = "1600 Amphitheatre Pkwy\nAttribution 1\nAttribution 2",
                position = googleplex
            )
        )
    }

    @Test
    fun toPlaceItemUiModel_withMissingLocation_returnsNull() {
        val place = Place.builder()
            .setDisplayName("No Location Place")
            .setFormattedAddress("Nowhere")
            .build()

        val uiModel = toPlaceItemUiModel(place, "Default Title")

        check(uiModel == null)
    }

    @Test
    fun toPlaceItemUiModel_withMissingDisplayName_usesDefaultTitle() {
        val place = Place.builder()
            .setFormattedAddress("1600 Amphitheatre Pkwy")
            .setLocation(googleplex)
            .build()

        val uiModel = toPlaceItemUiModel(place, "Default Title")

        check(
            uiModel == PlaceItemUiModel(
                title = "Default Title",
                snippet = "1600 Amphitheatre Pkwy",
                position = googleplex
            )
        )
    }

    @Test
    fun demoEnum_containsExpectedDemos() {
        val demos = Demo.values()
        check(
            demos.size == 3 &&
                Demo.DETAILS_FRAGMENT_DEMO.activity == DetailsActivity::class.java &&
                Demo.DETAILS_FRAGMENT_DEMO.title == R.string.details_demo_title &&
                Demo.DETAILS_FRAGMENT_DEMO.description == R.string.details_demo_description &&
                Demo.AUTOCOMPLETE_FRAGMENT_DEMO.activity == AutocompleteActivity::class.java &&
                Demo.AUTOCOMPLETE_FRAGMENT_DEMO.title == R.string.autocomplete_fragment_demo_title &&
                Demo.AUTOCOMPLETE_FRAGMENT_DEMO.description == R.string.autocomplete_fragment_demo_description &&
                Demo.CURRENT_FRAGMENT_DEMO.activity == CurrentPlaceActivity::class.java &&
                Demo.CURRENT_FRAGMENT_DEMO.title == R.string.current_demo_title &&
                Demo.CURRENT_FRAGMENT_DEMO.description == R.string.current_demo_description
        )
    }

    @Test
    fun currentPlaceActivity_onMapReadyAndSetPlaceOnMap_updatesCameraAndMarker() {
        val activity = CurrentPlaceActivity()
        val lastKnownLocationField = CurrentPlaceActivity::class.java.getDeclaredField(
            "lastKnownLocation"
        ).apply {
            isAccessible = true
        }
        lastKnownLocationField.set(activity, sydneyOperaHouse)

        val map = createTestMap(width = 200, height = 200)
        activity.onMapReady(map)

        map.assertMap(
            (map.cameraTarget distanceTo sydneyOperaHouse) <= 5.meters &&
                map.cameraZoom == 15f &&
                sydneyOperaHouse in map.visibleBounds
        )

        val setPlaceMethod = CurrentPlaceActivity::class.java.getDeclaredMethod(
            "setPlaceOnMap",
            PlaceItemUiModel::class.java
        ).apply {
            isAccessible = true
        }
        val googleplexUiModel = PlaceItemUiModel(
            title = "Googleplex",
            snippet = "1600 Amphitheatre Pkwy",
            position = googleplex
        )
        setPlaceMethod.invoke(activity, googleplexUiModel)

        map.assertMap(
            (map.cameraTarget distanceTo googleplex) <= 5.meters &&
                map.cameraZoom == 15f &&
                googleplex in map.visibleBounds &&
                map.markers.size == 1
        )

        val marker = map.findMarker("Googleplex")!!
        marker.assertMarker(
            (marker.position distanceTo googleplex) <= 1.meters &&
                marker.title == "Googleplex" &&
                marker.snippet == "1600 Amphitheatre Pkwy" &&
                marker.isVisible
        )
    }

    /**
     * Verifies that the coroutine code snippets quoted in the tutorial compile against
     * the Places SDK for Android 6.0.2 KTX extensions.
     */
    @Suppress("UNUSED_VARIABLE", "unused")
    private fun verifyCoroutineSnippetsCompile(
        lifecycleScope: CoroutineScope,
        placesClient: PlacesClient,
        fusedLocationClient: FusedLocationProviderClient,
        placeId: String,
        sessionToken: AutocompleteSessionToken?
    ) {
        // [START places_101_kotlin_coroutine_fetch_place]
        val placeFields = listOf(
            Place.Field.DISPLAY_NAME,
            Place.Field.ID,
            Place.Field.LOCATION,
            Place.Field.FORMATTED_ADDRESS
        )

        lifecycleScope.launch {
            try {
                val response = placesClient.awaitFetchPlace(placeId, placeFields) {
                    this.sessionToken = sessionToken
                }
                val place = response.place
            } catch (e: Exception) {
                // Handle API errors
            }
        }
        // [END places_101_kotlin_coroutine_fetch_place]

        // [START places_101_kotlin_coroutine_search_nearby]
        lifecycleScope.launch {
            val location = fusedLocationClient.lastLocation.await()
            val center = LatLng(location.latitude, location.longitude)
            val circle = CircularBounds.newInstance(center, /* radius = */ 500.0)

            val response = placesClient.awaitSearchNearby(circle, placeFields) {
                this.maxResultCount = 10
            }
            val places = response.places
        }
        // [END places_101_kotlin_coroutine_search_nearby]
    }
}
