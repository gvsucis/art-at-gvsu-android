package edu.gvsu.art.client.repository

import edu.gvsu.art.client.Tour
import edu.gvsu.art.client.api.TourSearchDetail
import edu.gvsu.art.client.api.TourSearchResult
import org.junit.Test
import java.net.URL
import kotlin.test.assertEquals

internal class TourSearchResultParsingExtTest {
    @Test
    fun it_converts_search_result_to_tour_stops() {
        val tourSearchResult = TourSearchResult(
            ok = true,
            tourSearchDetails = mutableListOf(
                TourSearchDetail(
                    tour_id = 1,
                    tour_name = "Outside Sculpture (Allendale)",
                    media_large_url = "https://artgallery.gvsu.edu/admin/media/collectiveaccess/images/3/5/6/8/65121_ca_attribute_values_value_blob_356821_large.jpg",
                ),
                TourSearchDetail(
                    tour_id = 3,
                    tour_name = "GVSU Favorites (Pew Campus)",
                    media_large_url = "https://artgallery.gvsu.edu/admin/media/collectiveaccess/images/3/5/7/6/32710_ca_attribute_values_value_blob_357643_large.jpg",
                )
            )
        )

        val expected = listOf(
            Tour(
                id = "1",
                name = "Outside Sculpture (Allendale)",
                mediaLarge = URL("https://artgallery.gvsu.edu/admin/media/collectiveaccess/images/3/5/6/8/65121_ca_attribute_values_value_blob_356821_large.jpg"),
            ),
            Tour(
                id = "3",
                name = "GVSU Favorites (Pew Campus)",
                mediaLarge = URL("https://artgallery.gvsu.edu/admin/media/collectiveaccess/images/3/5/7/6/32710_ca_attribute_values_value_blob_357643_large.jpg"),
            )
        )

        assertEquals(actual = tourSearchResult.toDomainModel, expected = expected)
    }
}
