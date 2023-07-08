package edu.gvsu.art.client

import java.net.URL

data class Location(
    val id: String = "",
    val name: String = "",
    val mediaMediumURL: URL? = null,
    val mediaLargeURL: URL? = null,
    val locations: List<Location> = emptyList(),
    val artworks: List<Artwork> = emptyList(),
)
