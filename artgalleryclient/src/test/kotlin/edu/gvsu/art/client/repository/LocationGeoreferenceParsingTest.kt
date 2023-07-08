package edu.gvsu.art.client.repository

import edu.gvsu.art.client.LatLng
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class LocationGeoreferenceParsingTest {
    @Test
    fun test_parseLocationGeoreference_it_returns_nil_on_empty_georeference() {
        val result = parseLocationGeoreference("")
        assertNull(result)
    }

    @Test
    fun test_parseLocationGeoreference_it_strips_extra_terminating_chars() {
        val expectedLatitude = "42.9000"
        val expectedLongitude = "42.10"
        val result =
            parseLocationGeoreference("$expectedLatitude,$expectedLongitude\\") // string terminates with unexpected token
        assertEquals(actual = result,
            expected = LatLng(expectedLatitude.toDouble(), expectedLongitude.toDouble()))
    }

    @Test
    fun test_parseLocationGeoreference_it_returns_first_pair_on_multiple_matches() {
        val expectedLatitude = "42.901"
        val expectedLongitude = "-85.886"

        val result = parseLocationGeoreference("[42.901,-85.886] 42.901,-85.886")

        assertEquals(actual = result,
            expected = LatLng(expectedLatitude.toDouble(), expectedLongitude.toDouble()))
    }

    @Test
    fun test_parseLocationGeoreference_it_handles_pair_brackets_in_any_order() {
        val expectedLatitude = "42.96003"
        val expectedLongitude = "-85.68134"
        val result =
            parseLocationGeoreference("$expectedLatitude, $expectedLongitude [$expectedLatitude,$expectedLongitude]")
        assertEquals(actual = result,
            expected = LatLng(expectedLatitude.toDouble(), expectedLongitude.toDouble()))
    }

    @Test
    fun test_parseLocationGeoreference_it_returns_coordinates_on_valid_match() {
        val latitude = 42.962858349348
        val longitude = -85.886878535968

        val (lat, long) = parseLocationGeoreference("[${latitude},${longitude}]")!!
        assertEquals(actual = lat, expected = latitude)
        assertEquals(actual = long, expected = longitude)
    }

    @Test
    fun test_parseLocationGeoreference_it_returns_null_on_uneven_pairs() {
        val latitude = "42.901"
        val longitude = "-85.886"

        val result = parseLocationGeoreference("[$latitude,$longitude] $latitude")
        assertNull(result)
    }

    @Test
    fun it_rejects_invalid_coordinates() {
        // Edge case. Tokenizer will parse lat=1.0 and long=49401.0 instead of 42.964,-85.887
        val location = "{1 N Campus Dr, Allendale Charter Township, MI 49401, USA [42.964,-85.887]"
        assertNull(parseLocationGeoreference(location))
    }
}
