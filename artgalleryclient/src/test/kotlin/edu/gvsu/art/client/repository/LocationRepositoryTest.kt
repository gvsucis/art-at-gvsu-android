package edu.gvsu.art.client.repository

import edu.gvsu.art.client.api.ArtGalleryClient
import edu.gvsu.art.client.api.LocationDetail
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertContains
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LocationRepositoryTest {
    @ExperimentalCoroutinesApi
    @Test
    fun it_returns_a_location_from_gallery_client() = runTest {
        val locationDetail = LocationDetail(
            ok = true,
            access = "1",
            location_id = 35,
            location_name = "Lake Superior Hall",
            location_description = "",
            media_large_url = "",
            media_medium_url = "",
            media_small_url = "",
            media_icon = "https://artgallery.gvsu.edu/admin/media/collectiveaccess/images/0/31698_ca_storage_locations_icon_35_icon.jpg",
            child_location_id = "643;644",
            child_location_name = "1st Floor (LSH);2nd Floor (LSH)",
            location_georeference = "[42.961958,-85.886727]",
            related_objects = "3676 / Companions / https://artgallery.gvsu.edu/admin/media/collectiveaccess/images/7/1/68059_ca_object_representations_media_7184_small.jpg;4447 / Galeria Plakata Krakow / https://artgallery.gvsu.edu/admin/media/collectiveaccess/images/6/8/25236_ca_object_representations_media_6859_small.jpg;15856 / Untitled / https://artgallery.gvsu.edu/admin/media/collectiveaccess/images/1/7/7/63188_ca_object_representations_media_17712_small.jpg;15857 / Untitled / https://artgallery.gvsu.edu/admin/media/collectiveaccess/images/1/7/7/73795_ca_object_representations_media_17713_small.jpg;15858 / Untitled / https://artgallery.gvsu.edu/admin/media/collectiveaccess/images/1/7/7/1161_ca_object_representations_media_17714_small.jpg"
        )
        val client = mockk<ArtGalleryClient>()
        coEvery { client.fetchLocationDetail("35") } returns locationDetail
        val repository = DefaultLocationRepository(client = client)

        val location = repository.find("35").getOrThrow()

        assertEquals(expected = "Lake Superior Hall", actual = location.name)
        assertEquals(expected = "35", actual = location.id)
        assertTrue(location.locations.size == 2, "Child location count does not match")
        assertTrue(location.artworks.size == 5, "Artwork count does not match")
    }
}
