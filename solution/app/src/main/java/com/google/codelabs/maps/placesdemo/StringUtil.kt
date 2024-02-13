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
import com.google.android.libraries.places.api.model.PlaceLikelihood
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse

internal const val FIELD_SEPARATOR = "\n\t"
internal const val RESULT_SEPARATOR = "\n---\n\t"

fun FetchPlaceResponse.prettyPrint(): String {
    val raw = false
    val response = this

    return buildString {
        append("Fetch Place Result:")
        append(RESULT_SEPARATOR)
        if (raw) {
            append(response.place)
        } else {
            append(response.place.prettyPrint())
        }
    }
}

fun FindCurrentPlaceResponse.prettyPrint(): String {
    val raw = false
    val response = this

    return buildString {
        append(response.placeLikelihoods.size)
        append(" Current Place Results:")
        if (raw) {
            append(RESULT_SEPARATOR)
            append(response.placeLikelihoods.joinToString(RESULT_SEPARATOR))
        } else {
            if (response.placeLikelihoods.isNotEmpty()) {
                append(RESULT_SEPARATOR)
            }
            append(
                response.placeLikelihoods.joinToString(RESULT_SEPARATOR) { placeLikelihood ->
                    placeLikelihood.prettyPrint()
                }
            )
        }
    }
}

internal fun prettyPrintAutocompleteWidget(place: Place, raw: Boolean): String {
    return buildString {
        append("Autocomplete Widget Result:")
        append(RESULT_SEPARATOR)
        if (raw) {
            append(place)
        } else {
            append(place.prettyPrint())
        }
    }
}

private fun PlaceLikelihood.prettyPrint() =
    "Likelihood: $likelihood${FIELD_SEPARATOR}Place: ${place.prettyPrint()}"

private fun LatLng.prettyPrint() = "$latitude, $longitude"

private fun Place.prettyPrint() = "$name ($id) is located at ${latLng?.prettyPrint()} ($address)"
