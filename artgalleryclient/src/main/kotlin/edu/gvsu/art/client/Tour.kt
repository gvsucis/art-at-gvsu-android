package edu.gvsu.art.client

import java.net.URL

data class Tour(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val mediaIcon: URL? = null,
    val mediaLarge: URL? = null,
    val mediaMedium: URL? = null,
    val mediaSmall: URL? = null,
    val stops: List<TourStop> = emptyList(),
)
