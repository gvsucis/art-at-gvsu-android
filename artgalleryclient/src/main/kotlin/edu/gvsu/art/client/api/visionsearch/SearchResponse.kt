package edu.gvsu.art.client.api.visionsearch

data class SearchResponse(
    val results: List<ImageResult> = emptyList()
)
