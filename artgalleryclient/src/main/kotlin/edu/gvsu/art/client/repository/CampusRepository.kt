package edu.gvsu.art.client.repository

import edu.gvsu.art.client.Location
import edu.gvsu.art.client.api.ArtGalleryClient
import edu.gvsu.art.client.api.CampusSearchResult
import edu.gvsu.art.client.common.optionalURL
import edu.gvsu.art.client.common.request

interface CampusRepository {
    suspend fun all(): Result<List<Location>>
}

class DefaultCampusRepository(
    private val client: ArtGalleryClient,
) : CampusRepository {
    override suspend fun all(): Result<List<Location>> {
        return request { client.fetchCampuses() }.fold(
            onSuccess = { Result.success(it.toDomainModel) },
            onFailure = { Result.failure(it) }
        )
    }
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
