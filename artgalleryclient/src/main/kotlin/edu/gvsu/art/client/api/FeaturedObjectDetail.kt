package edu.gvsu.art.client.api

data class FeaturedObjectDetail(
    var access: String? = "0",
    var object_id: Int?,
    var idno: String? = "",
    var media_icon_url: String? = "",
    var media_large_url: String? = "",
    var media_medium_url: String? = "",
    var media_small_url: String? = "",
    var reps: String? = "",
    var object_name: String? = "",
    var entity_id: String? = "",
    var entity_name: String? = "",
    var historical_context: String? = "",
    var work_description: String? = "",
    var ar_digital_asset: String? = "",
)

