package edu.gvsu.art.client

enum class ArtworkCollection(val slug: String) {
    FeaturedArt("featured_art"),
    FeaturedAR("featured_ar");

    companion object {
        fun find(slug: String): ArtworkCollection? {
            return entries.find { it.slug == slug }
        }
    }
}
