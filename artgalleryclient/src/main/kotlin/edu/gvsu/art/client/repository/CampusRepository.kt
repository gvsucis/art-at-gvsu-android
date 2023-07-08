package edu.gvsu.art.client.repository

import com.squareup.moshi.Moshi
import edu.gvsu.art.client.Location
import edu.gvsu.art.client.api.ArtGalleryClient
import edu.gvsu.art.client.api.CampusSearchResult
import edu.gvsu.art.client.api.GalleryConverter.moshi
import edu.gvsu.art.client.common.optionalURL
import edu.gvsu.art.client.data.Campuses
import edu.gvsu.art.client.data.CampusesQueries
import edu.gvsu.art.client.common.request
import edu.gvsu.art.db.ArtGalleryDatabase

interface CampusRepository {
    suspend fun all(): Result<List<Location>>
}

class DefaultCampusRepository(
    private val database: ArtGalleryDatabase,
    private val client: ArtGalleryClient,
) :CampusRepository {
    override suspend fun all(): Result<List<Location>> {
        val campuses = table.findLatest().executeAsOneOrNull()

        if (campuses != null && isFreshCache(campuses.created_at)) {
            return Result.success(convertFromLocations(moshi, campuses))
        }

        return request { client.fetchCampuses() }.fold(
            onSuccess = { cacheAndFind(it) },
            onFailure = { Result.failure(it) }
        )
    }

    private fun cacheAndFind(result: CampusSearchResult): Result<List<Location>> {
        database.transaction {
            table.deleteAll()
            table.insert(convertToCampusPayload(moshi, result))
        }

        return try {
            val record = table.findLatest().executeAsOne()
            Result.success(convertFromLocations(moshi, record))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private val table: CampusesQueries
        get() = database.campusesQueries
}

private val CampusSearchResult.toDomainModel: List<Location>
    get() = locationSearchDetails.map { locationDetail ->
        Location(
             id = locationDetail.location_id!!.toString(),
             name = locationDetail.location_name ?: "",
             mediaMediumURL = optionalURL(locationDetail.media_medium_url),
             mediaLargeURL = optionalURL(locationDetail.media_large_url),
        )
    }.sortedBy { it.id }

private fun convertFromLocations(moshi: Moshi, featured: Campuses): List<Location> {
    val jsonAdapter = moshi.adapter(CampusSearchResult::class.java)
    return jsonAdapter.fromJson(featured.payload)?.toDomainModel ?: listOf()
}

private fun convertToCampusPayload(moshi: Moshi, result: CampusSearchResult): String {
    val jsonAdapter = moshi.adapter(CampusSearchResult::class.java)
    return jsonAdapter.toJson(result)
}
