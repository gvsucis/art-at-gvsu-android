package edu.gvsu.art.client.api

data class TourSearchDetail(
    val access: String = "0",
    val tour_id: Int?,
    val tour_name: String? = "",
    val tour_description: String? = "",
    val media_icon: String? = null,
    val media_large_url: String? = null,
    val media_medium_url: String? = null,
    val media_small_url: String? = null,
    val tour_stops_id: String? = null,
    val tour_stops_name: String? = null
)
