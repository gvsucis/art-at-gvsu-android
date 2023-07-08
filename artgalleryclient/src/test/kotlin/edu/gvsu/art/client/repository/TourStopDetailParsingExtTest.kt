package edu.gvsu.art.client.repository

import edu.gvsu.art.client.api.TourStopDetail
import org.junit.Test
import java.net.URL
import kotlin.test.assertEquals

internal class TourStopDetailParsingExtTest {
    @Test
    fun it_returns_a_single_media_url() {
        val stop = TourStopDetail(
            stop_media = "https://artgallery.gvsu.edu/admin/media/collectiveaccess/images/9/7/94671_ca_object_representations_media_9726_small.jpg"
        )
        val media = stop.toDomainModel.media
        assertEquals(
            actual = media,
            expected = URL("https://artgallery.gvsu.edu/admin/media/collectiveaccess/images/9/7/94671_ca_object_representations_media_9726_small.jpg")
        )
    }

    @Test
    fun it_parses_the_first_valid_media_url() {
        val stop = TourStopDetail(
            stop_media = "/admin/media/collectiveaccess/images/8/44219_ca_object_representations_media_871_small.jpg; https://artgallery.gvsu.edu/admin/media/collectiveaccess/images/9/7/94671_ca_object_representations_media_9726_small.jpg"
        )
        val media = stop.toDomainModel.media
        assertEquals(
            actual = media,
            expected = URL("https://artgallery.gvsu.edu/admin/media/collectiveaccess/images/9/7/94671_ca_object_representations_media_9726_small.jpg")
        )
    }
}
