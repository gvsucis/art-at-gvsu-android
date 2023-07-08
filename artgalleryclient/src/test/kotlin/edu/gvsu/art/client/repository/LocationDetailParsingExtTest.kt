package edu.gvsu.art.client.repository

import edu.gvsu.art.client.Artwork
import edu.gvsu.art.client.Location
import edu.gvsu.art.client.api.LocationDetail
import org.junit.Test
import java.net.URL
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LocationDetailParsingExtTest {
    @Test
    fun it_zips_child_location_ids_and_names_into_locations() {
        val locationDetail = LocationDetail(
            child_location_id = "606;607;608",
            child_location_name = "Ground Floor (JHZ);1st Floor (JHZ);2nd Floor (JHZ)"
        )

        val expectedLocations = listOf(
            Location(id = "606", name = "Ground Floor (JHZ)"),
            Location(id = "607", name = "1st Floor (JHZ)"),
            Location(id = "608", name = "2nd Floor (JHZ)"),
        )

        assertEquals(actual = locationDetail.parseChildLocations(), expected = expectedLocations)
    }

    @Test
    fun test_it_returns_an_empty_list_for_blank_child_ids_and_names() {
        val locationDetail = LocationDetail(
            child_location_id = "",
            child_location_name = ""
        )

        assertTrue(locationDetail.parseChildLocations().isEmpty())
    }

    @Test
    fun it_splits_related_objects_into_artworks() {
        val related_objects =
            "3170 / Heaven and Earth / http://artgallery.gvsu.edu/admin/media/collectiveaccess/images/1/0/0/8999_ca_object_representations_media_10086_small.jpg;3171 / Transformational Link / http://artgallery.gvsu.edu/admin/media/collectiveaccess/images/7/3/98107_ca_object_representations_media_7384_small.jpg"

        val locationDetail = LocationDetail(
            related_objects = related_objects
        )

        val expectedArtworks = listOf(
            Artwork(
                id = "3170",
                name = "Heaven and Earth",
                thumbnail = URL("http://artgallery.gvsu.edu/admin/media/collectiveaccess/images/1/0/0/8999_ca_object_representations_media_10086_small.jpg")
            ),
            Artwork(
                id = "3171",
                name = "Transformational Link",
                thumbnail = URL("http://artgallery.gvsu.edu/admin/media/collectiveaccess/images/7/3/98107_ca_object_representations_media_7384_small.jpg")
            )
        )

        assertEquals(actual = locationDetail.parseRelatedObjects(), expected = expectedArtworks)
    }
}
