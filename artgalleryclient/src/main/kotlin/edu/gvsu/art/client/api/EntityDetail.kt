package edu.gvsu.art.client.api

data class EntityDetail(
    val ok: Boolean? = false,
    val access: String? = "0",
    val idno: String? = "",
    val entity_id: Int? = null,
    val display_label: String? = "",
    val nationality: String? = "",
    val life_dates: String? = "",
    val biography: String? = "",
    val related_objects: String? = "",
)
