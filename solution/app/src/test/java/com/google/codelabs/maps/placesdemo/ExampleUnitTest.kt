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

import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.SearchNearbyResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying StringUtil formatting and Demo configuration.
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
}
