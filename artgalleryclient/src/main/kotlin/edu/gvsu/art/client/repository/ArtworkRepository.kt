package edu.gvsu.art.client.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOne
import edu.gvsu.art.client.Artwork
import edu.gvsu.art.client.api.ArtGalleryClient
import edu.gvsu.art.client.api.ObjectDetail
import edu.gvsu.art.client.data.ArtworksQueries
import edu.gvsu.art.client.common.request
import edu.gvsu.art.db.ArtGalleryDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull

interface ArtworkRepository {
    suspend fun find(id: String): Result<Artwork>

    fun insert(objectDetail: ObjectDetail)
}

class DefaultArtworkRepository(
    val database: ArtGalleryDatabase,
    val client: ArtGalleryClient,
) : ArtworkRepository {
    override suspend fun find(id: String): Result<Artwork> {
        val artwork = table.findByID(id = id).executeAsOneOrNull()

        if (artwork != null && isFreshCache(artwork.created_at)) {
            return Result.success(artwork.toDomainModel)
        }

        return request { client.fetchObjectDetail(id) }.fold(
            onSuccess = { cacheAndFind(it) },
            onFailure = { Result.failure(Throwable("object detail was missing. id=${id}")) }
        )
    }

    private suspend fun cacheAndFind(objectDetail: ObjectDetail): Result<Artwork> {
        insert(objectDetail)

        return try {
            val record = table
                .findByID(objectDetail.object_id!!.toString())
                .asFlow()
                .mapToOne(Dispatchers.IO)
                .firstOrNull() ?: return Result.failure(Error("Artwork not found"))

            Result.success(record.toDomainModel)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun insert(objectDetail: ObjectDetail) {
        val latLng = objectDetail.parseLocationGeoreference()
        table.insert(
            id = objectDetail.object_id!!.toString(),
            is_public = if (objectDetail.access == "1") 1 else 0,
            media_representations = objectDetail
                .parseMediaRepresentations()
                .joinToString(",") { it.toString() },
            name = objectDetail.object_name,
            artist_id = objectDetail.entity_id,
            artist_name = objectDetail.entity_name,
            historical_context = objectDetail.historical_context,
            work_description = objectDetail.work_description,
            work_date = objectDetail.work_date,
            work_medium = objectDetail.work_medium,
            location_id = objectDetail.location_id,
            location = objectDetail.location,
            identifier = objectDetail.idno,
            credit_line = objectDetail.credit_line,
            media_small_url = objectDetail.media_small_url,
            media_medium_url = objectDetail.media_medium_url,
            media_large_url = objectDetail.media_large_url,
            thumbnail_url = objectDetail.media_icon_url,
            location_latitude = latLng?.latitude,
            location_longitude = latLng?.longitude,
            related_objects = objectDetail.related_objects,
            ar_digital_asset_url = objectDetail.ar_digital_asset
        )
    }

    private val table: ArtworksQueries
        get() = database.artworksQueries
}
