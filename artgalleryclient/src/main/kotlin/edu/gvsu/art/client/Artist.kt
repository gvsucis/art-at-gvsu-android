package edu.gvsu.art.client

data class Artist(
    val id: String = "",
    val isPublic: Boolean = true,
    val identifier: String = "",
    val name: String = "",
    val nationality: String = "",
    val lifeDates: String = "",
    val biography: String = "",
    val relatedWorks: List<Artwork> = listOf()
)
