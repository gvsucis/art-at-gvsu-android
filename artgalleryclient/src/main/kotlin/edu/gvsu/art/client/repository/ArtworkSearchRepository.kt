package edu.gvsu.art.client.repository

import edu.gvsu.art.client.Artwork
import edu.gvsu.art.client.ArtworkCollection
import edu.gvsu.art.client.api.ArtGalleryClient
import edu.gvsu.art.client.api.ArtworkSearchResult
import edu.gvsu.art.client.common.optionalURL
import edu.gvsu.art.client.common.request

interface ArtworkSearchRepository {
    suspend fun search(query: String): Result<List<Artwork>>
    suspend fun searchCollection(collection: ArtworkCollection): Result<List<Artwork>>
}

class DefaultArtworkSearchRepository(
    private val client: ArtGalleryClient,
) : ArtworkSearchRepository {
    override suspend fun search(query: String): Result<List<Artwork>> {
        return request { client.fetchArtworkSearch(query = query) }.fold(
            onSuccess = { Result.success(it.toDomainArtworks) },
            onFailure = { Result.failure(it) }
        )
    }

    override suspend fun searchCollection(collection: ArtworkCollection): Result<List<Artwork>> {
        return search(query = collection.slug)
    }
}

private val ArtworkSearchResult.toDomainArtworks: List<Artwork>
    get() = objectDetails.map { objectDetail ->
        Artwork(
            id = objectDetail.object_id!!.toString(),
            isPublic = objectDetail.access == "1",
            name = objectDetail.object_name ?: "",
            artistID = objectDetail.entity_id ?: "",
            artistName = objectDetail.entity_name ?: "",
            historicalContext = objectDetail.historical_context ?: "",
            workDescription = objectDetail.work_description ?: "",
            identifier = objectDetail.idno ?: "",
            mediaMedium = optionalURL(objectDetail.media_medium_url),
            mediaLarge = optionalURL(objectDetail.media_large_url),
            thumbnail = optionalURL(objectDetail.media_small_url),
            arDigitalAssetURL = optionalURL(objectDetail.ar_digital_asset)
        )
    }
