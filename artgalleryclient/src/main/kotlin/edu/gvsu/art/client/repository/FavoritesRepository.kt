package edu.gvsu.art.client.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import edu.gvsu.art.client.Artwork
import edu.gvsu.art.client.common.optionalURL
import edu.gvsu.art.client.data.FavoriteArtworks
import edu.gvsu.art.db.ArtGalleryDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface FavoritesRepository {
    fun exists(artworkID: String): Boolean
    fun toggle(artwork: Artwork): Boolean
    fun add(artwork: Artwork)
    fun all(): Flow<List<Artwork>>
}

class DefaultFavoritesRepository(private val database: ArtGalleryDatabase) : FavoritesRepository {
    override fun toggle(artwork: Artwork): Boolean {
        if (exists(artwork.id)) {
            table.delete(artwork.id)
        } else {
            insertRow(artwork)
        }

        return exists(artwork.id)
    }

    override fun exists(artworkID: String): Boolean {
        return database.favoriteArtworksQueries.favoriteExists(artworkID).executeAsOneOrNull()
            ?: false
    }

    override fun add(artwork: Artwork) {
        insertRow(artwork)
    }

    override fun all(): Flow<List<Artwork>> {
        return table
            .findAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows -> rows.map(::toArtwork) }
    }

    private fun insertRow(artwork: Artwork) {
        table.insert(
            artwork_id = artwork.id,
            title = artwork.name,
            artist_name = artwork.artistName,
            media_large_url = artwork.mediaLarge?.toString(),
        )
    }

    private val table
        get() = database.favoriteArtworksQueries
}

private fun toArtwork(row: FavoriteArtworks): Artwork = Artwork(
    id = row.artwork_id,
    name = row.title.orEmpty(),
    artistName = row.artist_name.orEmpty(),
    mediaLarge = optionalURL(row.media_large_url),
)
