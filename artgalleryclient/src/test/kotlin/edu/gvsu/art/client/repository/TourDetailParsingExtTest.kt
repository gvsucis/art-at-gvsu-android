package edu.gvsu.art.client.repository

import edu.gvsu.art.client.Tour
import edu.gvsu.art.client.TourStop
import edu.gvsu.art.client.api.TourDetail
import org.junit.Test
import java.net.URL
import kotlin.test.assertEquals

internal class TourDetailParsingExtTest {
    @Test
    fun it_parses_a_tour_detail_to_a_tour() {
        val tourDetail = TourDetail(
            ok = true,
            access = "1",
            tour_id = 3,
            tour_name = "GVSU Favorites (Pew Campus)",
            tour_description = "Fifty artworks were selected on by the GVSU community",
            media_icon = "https://artgallery.gvsu.edu/admin/media/collectiveaccess/images/0/88912_ca_tours_icon_3_icon.jpg",
            media_large_url = "<img src='https://artgallery.gvsu.edu/admin/media/collectiveaccess/images/3/5/7/6/32710_ca_attribute_values_value_blob_357643_large.jpg' width='700' height='525' alt='GVSU Favorites (Pew Campus)' />",
            media_medium_url = "<img src='https://artgallery.gvsu.edu/admin/media/collectiveaccess/images/3/5/7/6/66278_ca_attribute_values_value_blob_357643_medium.jpg' width='400' height='300' alt='GVSU Favorites (Pew Campus)' />",
            media_small_url = "<img src='https://artgallery.gvsu.edu/admin/media/collectiveaccess/images/3/5/7/6/4543_ca_attribute_values_value_blob_357643_small.jpg' width='240' height='180' alt='GVSU Favorites (Pew Campus)' />",
            tour_stops_id = "48;42;45",
            tour_stops_media = "",
            tour_stops_name = "A Brief Medical;Actuality #8;Both Sides of the Brain;"
        )

        val expected = Tour(
            id = "3",
            name = "GVSU Favorites (Pew Campus)",
            description = "Fifty artworks were selected on by the GVSU community",
            mediaIcon = URL("https://artgallery.gvsu.edu/admin/media/collectiveaccess/images/0/88912_ca_tours_icon_3_icon.jpg"),
            stops = listOf(
                TourStop(id = "48", name = "A Brief Medical"),
                TourStop(id = "42", name = "Actuality #8"),
                TourStop(id = "45", name = "Both Sides of the Brain")
            )
        )

        assertEquals(expected = expected, actual = tourDetail.toDomainModel)
    }
}
