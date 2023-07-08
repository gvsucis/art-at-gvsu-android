package edu.gvsu.art.client.api

data class LocationSearchDetail(
    val access: String = "0",
    val location_id: Int?,
    val location_name: String? = "",
    val location_description: String? = "",
    val media_large_url: String? = "",
    val media_medium_url: String? = "",
    val media_small_url: String? = "",
    val media_icon: String? = "",
    val child_location_id: String? = "",
    val child_location_name: String? = "",
    val location_georeference: String? = "",
    val related_objects: String? = "",
)
