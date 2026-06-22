package edu.gvsu.art.client.repository

import edu.gvsu.art.client.Artist
import edu.gvsu.art.client.api.ArtGalleryClient
import edu.gvsu.art.client.api.ArtistSearchResult
import edu.gvsu.art.client.api.EntityDetail
import edu.gvsu.art.client.common.request

interface ArtistRepository {
    suspend fun search(query: String, limit: Int? = null): Result<List<Artist>>
    suspend fun find(id: String): Result<Artist>
}

class DefaultArtistRepository(
    private val client: ArtGalleryClient,
) : ArtistRepository {
    override suspend fun search(query: String, limit: Int?): Result<List<Artist>> {
        return request { client.fetchArtistSearch(query = query, limit = limit) }.fold(
            onSuccess = {
                try {
                    Result.success(it.toDomainModel)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            },
            onFailure = { Result.failure(it) }
        )
    }

    override suspend fun find(id: String): Result<Artist> {
        return request { client.fetchEntityDetail(id) }.fold(
            onSuccess = { Result.success(it.toDomainModel) },
            onFailure = { Result.failure(Throwable("entity detail was missing. id=${id}")) }
        )
    }
}

internal val EntityDetail.toDomainModel: Artist
    get() = Artist(
        id = entity_id!!.toString(),
        isPublic = access == "1",
        identifier = idno ?: "",
        name = display_label ?: "",
        nationality = nationality ?: "",
        lifeDates = life_dates ?: "",
        biography = biography ?: "",
        relatedWorks = parseRelatedObjects(related_objects)
    )

val ArtistSearchResult.toDomainModel: List<Artist>
    get() = entityDetails.map {
        Artist(
            id = it.entity_id!!.toString(),
            isPublic = it.access == "1",
            identifier = it.idno ?: "",
            name = it.display_label ?: "",
            nationality = it.nationality ?: "",
            lifeDates = it.life_dates ?: "",
            biography = it.biography ?: "",
            relatedWorks = parseRelatedObjects(it.related_objects)
        )
    }
