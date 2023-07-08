package edu.gvsu.art.client.repository

import edu.gvsu.art.client.Artist
import edu.gvsu.art.client.api.ArtGalleryClient
import edu.gvsu.art.client.api.ArtistSearchResult
import edu.gvsu.art.client.api.EntityDetail
import edu.gvsu.art.client.data.ArtistsQueries
import edu.gvsu.art.client.common.request
import edu.gvsu.art.db.ArtGalleryDatabase

interface ArtistRepository {
    suspend fun search(query: String): Result<List<Artist>>
    suspend fun find(id: String): Result<Artist>
}

class DefaultArtistRepository(
    private val database: ArtGalleryDatabase,
    private val client: ArtGalleryClient,
) : ArtistRepository {
    override suspend fun search(query: String): Result<List<Artist>> {
        return request { client.fetchArtistSearch(query = query) }.fold(
            onSuccess = { Result.success(it.toDomainModel) },
            onFailure = { Result.failure(it) }
        )
    }

    override suspend fun find(id: String): Result<Artist> {
        val artist = table.findByID(id).executeAsOneOrNull()
        if (artist != null && isFreshCache(artist.created_at)) {
            return Result.success(artist.toDomainModel)
        }

        return request { client.fetchEntityDetail(id) }.fold(
            onSuccess = { cacheAndFind(it) },
            onFailure = { Result.failure(Throwable("entity detail was missing. id=${id}")) }
        )
    }

    private fun cacheAndFind(entityDetail: EntityDetail): Result<Artist> {
        insert(entityDetail)

        return try {
            val record = table
                .findByID(entityDetail.entity_id!!.toString())
                .executeAsOne()
            Result.success(record.toDomainModel)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun insert(entityDetail: EntityDetail) {
        table.insert(
            id = entityDetail.entity_id!!.toString(),
            is_public = if (entityDetail.access == "1") 1 else 0,
            identifier = entityDetail.idno,
            name = entityDetail.display_label,
            nationality = entityDetail.nationality,
            life_dates = entityDetail.life_dates,
            biography = entityDetail.biography,
            related_works = entityDetail.related_objects
        )
    }

    val table: ArtistsQueries
        get() = database.artistsQueries
}


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
