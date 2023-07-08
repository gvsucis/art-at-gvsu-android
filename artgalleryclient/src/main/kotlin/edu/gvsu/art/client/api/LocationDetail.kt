package edu.gvsu.art.client.api

data class LocationDetail(
    var ok: Boolean = false,
    var access: String? = "0",
    var location_id: Int? = null,
    var location_name: String? = "",
    var location_description: String? = "",
    var media_large_url: String? = "",
    var media_medium_url: String? = "",
    var media_small_url: String? = "",
    var media_icon: String? = "",
    var child_location_id: String? = "",
    var child_location_name: String? = "",
    var location_georeference: String? = "",
    var related_objects: String? = "",
)
