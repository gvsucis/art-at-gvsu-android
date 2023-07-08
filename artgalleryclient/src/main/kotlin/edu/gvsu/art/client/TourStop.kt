package edu.gvsu.art.client

import java.net.URL

data class TourStop(
    val id: String = "",
    val name: String = "",
    val artworkID: String = "",
    val media: URL? = null,
    val location: LatLng? = null,
    val artwork: Artwork = Artwork()
)
