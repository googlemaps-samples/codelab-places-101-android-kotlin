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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying StringUtil formatting, PlaceItemUiModel mapping, and Demo configuration.
 */
class ExampleUnitTest {

    private fun buildSamplePlace(): Place =
        Place.builder()
            .setDisplayName("Sydney Opera House")
            .setId("ChIJ3S-JXmauEmsRUcIaWtf4MzE")
            .setLocation(LatLng(-33.8567844, 151.2152967))
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
        assertEquals(expected, formatted)
    }

    @Test
    fun searchNearbyResponse_prettyPrint_emptyList() {
        val response = SearchNearbyResponse.newInstance(emptyList())
        assertEquals("0 Current Place Results:", response.prettyPrint())
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
        assertEquals(expected, formatted)
    }

    @Test
    fun prettyPrintAutocompleteWidget_formatsPrettyAndRaw() {
        val place = buildSamplePlace()

        val pretty = prettyPrintAutocompleteWidget(place, raw = false)
        val expectedPretty =
            "Autocomplete Widget Result:$RESULT_SEPARATOR" +
                "Sydney Opera House (ChIJ3S-JXmauEmsRUcIaWtf4MzE) is located at " +
                "-33.8567844, 151.2152967 (Bennelong Point, Sydney NSW 2000, Australia)"
        assertEquals(expectedPretty, pretty)

        val raw = prettyPrintAutocompleteWidget(place, raw = true)
        assertTrue(raw.startsWith("Autocomplete Widget Result:$RESULT_SEPARATOR"))
        assertTrue(raw.contains("Sydney Opera House"))
    }

    @Test
    fun toPlaceItemUiModel_withAllFields_mapsExpectedUiModel() {
        val latLng = LatLng(37.4220, -122.0841)
        val place = Place.builder()
            .setDisplayName("Googleplex")
            .setFormattedAddress("1600 Amphitheatre Pkwy")
            .setAttributions(listOf("Attribution 1", "Attribution 2"))
            .setLocation(latLng)
            .build()

        val uiModel = toPlaceItemUiModel(place, "Default Title")

        assertEquals(
            PlaceItemUiModel(
                title = "Googleplex",
                snippet = "1600 Amphitheatre Pkwy\nAttribution 1\nAttribution 2",
                position = latLng
            ),
            uiModel
        )
    }

    @Test
    fun toPlaceItemUiModel_withMissingLocation_returnsNull() {
        val place = Place.builder()
            .setDisplayName("No Location Place")
            .setFormattedAddress("Nowhere")
            .build()

        val uiModel = toPlaceItemUiModel(place, "Default Title")

        assertNull(uiModel)
    }

    @Test
    fun toPlaceItemUiModel_withMissingDisplayName_usesDefaultTitle() {
        val latLng = LatLng(37.4220, -122.0841)
        val place = Place.builder()
            .setFormattedAddress("1600 Amphitheatre Pkwy")
            .setLocation(latLng)
            .build()

        val uiModel = toPlaceItemUiModel(place, "Default Title")

        assertEquals(
            PlaceItemUiModel(
                title = "Default Title",
                snippet = "1600 Amphitheatre Pkwy",
                position = latLng
            ),
            uiModel
        )
    }

    @Test
    fun demoEnum_containsExpectedDemos() {
        val demos = Demo.values()
        assertEquals(3, demos.size)

        assertEquals(DetailsActivity::class.java, Demo.DETAILS_FRAGMENT_DEMO.activity)
        assertEquals(R.string.details_demo_title, Demo.DETAILS_FRAGMENT_DEMO.title)
        assertEquals(R.string.details_demo_description, Demo.DETAILS_FRAGMENT_DEMO.description)

        assertEquals(AutocompleteActivity::class.java, Demo.AUTOCOMPLETE_FRAGMENT_DEMO.activity)
        assertEquals(R.string.autocomplete_fragment_demo_title, Demo.AUTOCOMPLETE_FRAGMENT_DEMO.title)
        assertEquals(R.string.autocomplete_fragment_demo_description, Demo.AUTOCOMPLETE_FRAGMENT_DEMO.description)

        assertEquals(CurrentPlaceActivity::class.java, Demo.CURRENT_FRAGMENT_DEMO.activity)
        assertEquals(R.string.current_demo_title, Demo.CURRENT_FRAGMENT_DEMO.title)
        assertEquals(R.string.current_demo_description, Demo.CURRENT_FRAGMENT_DEMO.description)
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
