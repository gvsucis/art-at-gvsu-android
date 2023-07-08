package edu.gvsu.art.client.repository

import com.squareup.moshi.Moshi
import edu.gvsu.art.client.Artwork
import edu.gvsu.art.client.api.ArtGalleryClient
import edu.gvsu.art.client.api.ArtworkSearchResult
import edu.gvsu.art.client.api.GalleryConverter.moshi
import edu.gvsu.art.client.common.optionalURL
import edu.gvsu.art.client.data.FeaturedArtworks
import edu.gvsu.art.client.data.FeaturedArtworksQueries
import edu.gvsu.art.client.common.request
import edu.gvsu.art.db.ArtGalleryDatabase

interface ArtworkSearchRepository {
    suspend fun search(query: String): Result<List<Artwork>>
    suspend fun featured(): Result<List<Artwork>>
}

class DefaultArtworkSearchRepository(
    private val database: ArtGalleryDatabase,
    private val client: ArtGalleryClient,
) : ArtworkSearchRepository {
    override suspend fun search(query: String): Result<List<Artwork>> {
        return request { client.fetchArtworkSearch(query = query) }.fold(
            onSuccess = { Result.success(it.toDomainArtworks) },
            onFailure = { Result.failure(it) }
        )
    }

    override suspend fun featured(): Result<List<Artwork>> {
        val featured = table.findLatest().executeAsOneOrNull()

        if (featured != null && isFreshCache(featured.created_at)) {
            return Result.success(convertFromFeaturedArt(moshi, featured))
        }

        return request { client.fetchFeaturedArt() }.fold(
            onSuccess = { cacheAndFindFeaturedArt(it) },
            onFailure = { Result.failure(it) }
        )
    }

    private fun cacheAndFindFeaturedArt(
        result: ArtworkSearchResult,
    ): Result<List<Artwork>> {
        database.transaction {
            table.deleteAll()
            table.insert(convertToFeaturedArtworkPayload(moshi, result))
        }

        return try {
            val record = table
                .findLatest()
                .executeAsOne()
            Result.success(convertFromFeaturedArt(moshi, record))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private val table: FeaturedArtworksQueries
        get() = database.featuredArtworksQueries
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
            thumbnail = optionalURL(objectDetail.media_small_url)
        )
    }

private fun convertFromFeaturedArt(moshi: Moshi, featured: FeaturedArtworks): List<Artwork> {
    val jsonAdapter = moshi.adapter(ArtworkSearchResult::class.java)
    return jsonAdapter.fromJson(featured.payload)?.toDomainArtworks ?: listOf()
}

private fun convertToFeaturedArtworkPayload(moshi: Moshi, result: ArtworkSearchResult): String {
    val jsonAdapter = moshi.adapter(ArtworkSearchResult::class.java)
    return jsonAdapter.toJson(result)
}
