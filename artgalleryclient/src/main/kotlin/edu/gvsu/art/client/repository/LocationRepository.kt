package edu.gvsu.art.client.repository

import edu.gvsu.art.client.Location
import edu.gvsu.art.client.api.ArtGalleryClient
import edu.gvsu.art.client.api.LocationDetail
import edu.gvsu.art.client.common.optionalURL
import edu.gvsu.art.client.common.request

interface LocationRepository {
    suspend fun find(locationID: String): Result<Location>
}

class DefaultLocationRepository(val client: ArtGalleryClient) : LocationRepository {
    override suspend fun find(locationID: String): Result<Location> {
        return request { client.fetchLocationDetail(id = locationID) }.fold(
            onSuccess = { Result.success(it.toDomainModel) },
            onFailure = { Result.failure(it) }
        )
    }
}

val LocationDetail.toDomainModel: Location
    get() = Location(
        id = location_id!!.toString(),
        name = location_name ?: "",
        mediaMediumURL = optionalURL(media_medium_url),
        mediaLargeURL = optionalURL(media_large_url),
        locations = parseChildLocations(),
        artworks = parseRelatedObjects()
    )
