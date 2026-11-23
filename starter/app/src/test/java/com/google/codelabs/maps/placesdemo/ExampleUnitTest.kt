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

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.SearchNearbyResponse
import com.google.maps.android.robolectric.annotation.EnableMapsShadows
import com.google.maps.android.robolectric.ktx.cameraTarget
import com.google.maps.android.robolectric.ktx.cameraZoom
import com.google.maps.android.robolectric.ktx.markers
import com.google.maps.android.robolectric.ktx.visibleBounds
import com.google.maps.android.robolectric.ktx.withTestMap
import com.google.maps.android.robolectric.powerassert.assertMap
import com.google.maps.android.robolectric.powerassert.assertMarker
import com.google.maps.android.robolectric.powerassert.contains
import com.google.maps.android.robolectric.powerassert.distanceTo
import com.google.maps.android.robolectric.powerassert.isInfoWindowShownFor
import com.google.maps.android.robolectric.powerassert.meters
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Headless JVM unit and Robolectric tests for the starter project using
 * `android-maps-testing` (`:robolectric-testing`) and Kotlin Power-Assert.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
@EnableMapsShadows
class ExampleUnitTest {

    private val sydneyOperaHouse = LatLng(-33.8567844, 151.2152967)

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
    fun searchNearbyResponse_prettyPrint_emptyAndPopulatedLists() {
        val emptyResponse = SearchNearbyResponse.newInstance(emptyList())
        check(emptyResponse.prettyPrint() == "0 Current Place Results:")

        val populatedResponse = SearchNearbyResponse.newInstance(listOf(buildSamplePlace()))
        val expected =
            "1 Current Place Results:$RESULT_SEPARATOR" +
                "Sydney Opera House (ChIJ3S-JXmauEmsRUcIaWtf4MzE) is located at " +
                "-33.8567844, 151.2152967 (Bennelong Point, Sydney NSW 2000, Australia)"
        check(populatedResponse.prettyPrint() == expected)
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
    fun demoEnum_startsEmptyInStarter() {
        check(Demo.values().isEmpty())
    }

    @Test
    fun testMapHarness_movesCameraAndAddsPlaceMarker() = withTestMap(width = 200, height = 200) {
        moveCamera(CameraUpdateFactory.newLatLngZoom(sydneyOperaHouse, 15f))

        val marker = addMarker(
            MarkerOptions()
                .position(sydneyOperaHouse)
                .title("Sydney Opera House")
                .snippet("Bennelong Point, Sydney NSW 2000, Australia")
        )!!.apply {
            showInfoWindow()
        }

        assertMap(
            (cameraTarget distanceTo sydneyOperaHouse) <= 5.meters &&
                cameraZoom == 15f &&
                sydneyOperaHouse in visibleBounds &&
                markers.size == 1 &&
                isInfoWindowShownFor("Sydney Opera House")
        )

        marker.assertMarker(
            (marker.position distanceTo sydneyOperaHouse) <= 1.meters &&
                marker.title == "Sydney Opera House" &&
                marker.snippet == "Bennelong Point, Sydney NSW 2000, Australia" &&
                marker.isVisible &&
                marker.isInfoWindowShown
        )
    }
}
