package edu.gvsu.art.client.repository

import edu.gvsu.art.client.Artwork
import edu.gvsu.art.client.api.ArtGalleryClient
import edu.gvsu.art.client.api.ObjectDetail
import edu.gvsu.art.client.common.asUrls
import edu.gvsu.art.client.common.optionalURL
import edu.gvsu.art.client.common.request

interface ArtworkRepository {
    suspend fun find(id: String): Result<Artwork>
}

class DefaultArtworkRepository(
    val client: ArtGalleryClient,
) : ArtworkRepository {
    override suspend fun find(id: String): Result<Artwork> {
        return request { client.fetchObjectDetail(id) }.fold(
            onSuccess = { Result.success(it.toDomainModel) },
            onFailure = { Result.failure(Throwable("object detail was missing. id=${id}")) }
        )
    }
}

internal val ObjectDetail.toDomainModel: Artwork
    get() = Artwork(
        id = object_id!!.toString(),
        isPublic = access == "1",
        mediaRepresentations = parseMediaRepresentations(),
        secondaryMedia = parseSecondaryMedia(secondary_media_reps, secondary_media_rep_thumbnails),
        name = object_name ?: "",
        artistID = entity_id ?: "",
        artistName = entity_name ?: "",
        historicalContext = historical_context ?: "",
        workDescription = work_description ?: "",
        workDate = work_date ?: "",
        workMedium = work_medium ?: "",
        locationID = location_id.orEmpty(),
        location = location ?: "",
        identifier = idno ?: "",
        creditLine = credit_line ?: "",
        locationGeoreference = parseLocationGeoreference(),
        relatedWorks = parseRelatedObjects(related_objects),
        mediaSmall = optionalURL(media_small_url),
        mediaMedium = optionalURL(media_medium_url),
        mediaLarge = optionalURL(media_large_url),
        thumbnail = optionalURL(media_icon_url),
        arDigitalAssetURL = optionalURL(ar_digital_asset),
    )
