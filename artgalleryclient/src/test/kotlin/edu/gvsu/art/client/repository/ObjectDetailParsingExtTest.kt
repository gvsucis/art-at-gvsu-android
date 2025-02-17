package edu.gvsu.art.client.repository

import edu.gvsu.art.client.Artwork
import edu.gvsu.art.client.LatLng
import edu.gvsu.art.client.api.ObjectDetail
import java.net.URL
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class ObjectDetailParsingExtTest {
    @Test
    fun test_parseLocationGeoreference_it_returns_nil_on_nil_georeference() {
        val objectDetail = ObjectDetail(object_id = 123, location_georeference = null)

        assertNull(objectDetail.parseLocationGeoreference())
    }

    @Test
    fun test_parseLocationGeoreference_it_returns_nil_on_empty_georeference() {
        val objectDetail = ObjectDetail(object_id = 123, location_georeference = "")
        assertNull(objectDetail.parseLocationGeoreference())
    }

    @Test
    fun test_parseLocationGeoreference_it_returns_nil_on_non_matching_georeference() {
        val expectedLatitude = "42.9000"
        val expectedLongitude = "42.10"

        val objectDetail =
            ObjectDetail(
                object_id = 123,
                location_georeference = "$expectedLatitude,$expectedLongitude\\"
            ) // string terminates with unexpected token
        assertEquals(
            actual = objectDetail.parseLocationGeoreference(),
            expected = LatLng(expectedLatitude.toDouble(), expectedLongitude.toDouble())
        )
    }


    @Test
    fun test_parseLocationGeoreference_it_returns_nil_on_multiple_matches() {
        val objectDetail =
            ObjectDetail(object_id = 123, location_georeference = "[42.901,-85.886] 42.901,-85.886")
        val expectedLatitude = "42.901"
        val expectedLongitude = "-85.886"

        assertEquals(
            actual = objectDetail.parseLocationGeoreference(),
            expected = LatLng(expectedLatitude.toDouble(), expectedLongitude.toDouble())
        )
    }


    @Test
    fun test_parseLocationGeoreference_it_returns_coordinates_on_valid_match() {
        val latitude = 42.962858349348
        val longitude = -85.886878535968

        val objectDetail =
            ObjectDetail(object_id = 123, location_georeference = "[${latitude},${longitude}]")
        val coordinates = objectDetail.parseLocationGeoreference()!!
        assertEquals(actual = coordinates.latitude, expected = latitude)
        assertEquals(actual = coordinates.longitude, expected = longitude)
    }

    @Test
    fun test_parseRelatedObjects_it_returns_a_single_object() {
        val relatedObjects =
            "3817 / Dutchman with Canal Boat / https://artgallery.gvsu.edu/admin/media/collectiveaccess/images/1/5/6/2171_ca_object_representations_media_15698_small.jpg,"
        val objectDetail = ObjectDetail(object_id = 123, related_objects = relatedObjects)

        val expectedArtworks = listOf(
            Artwork(
                id = "3817",
                name = "Dutchman with Canal Boat",
                thumbnail = URL("https://artgallery.gvsu.edu/admin/media/collectiveaccess/images/1/5/6/2171_ca_object_representations_media_15698_small.jpg")
            )
        )

        assertEquals(expected = expectedArtworks, actual = objectDetail.parseRelatedWorks())
    }
}
