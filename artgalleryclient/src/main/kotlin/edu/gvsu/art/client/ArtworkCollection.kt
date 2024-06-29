package edu.gvsu.art.client

enum class ArtworkCollection(val slug: String) {
    FeaturedArt("featured_art"),
    AR("AR_Alten_2022");

    companion object {
        fun find(slug: String): ArtworkCollection? {
            return entries.find { it.slug == slug }
        }
    }
}
