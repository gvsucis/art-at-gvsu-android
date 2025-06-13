package edu.gvsu.art.client.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import edu.gvsu.art.client.Artwork
import edu.gvsu.art.db.ArtGalleryDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    fun exists(artworkID: String): Boolean
    fun toggle(artworkID: String): Boolean
    fun add(artworkID: String)
    fun all(): Flow<List<Artwork>>
}

class DefaultFavoritesRepository(private val database: ArtGalleryDatabase) : FavoritesRepository {
    override fun toggle(artworkID: String): Boolean {
        if (exists(artworkID)) {
            table.delete(artworkID)
        } else {
            table.insert(artworkID)
        }

        return exists(artworkID)
    }

    override fun exists(artworkID: String): Boolean {
        return database.favoriteArtworksQueries.favoriteExists(artworkID).executeAsOneOrNull()
            ?: false
    }

    override fun add(artworkID: String) {
        table.insert(artworkID)
    }

    override fun all(): Flow<List<Artwork>> {
        return table
            .findAll(::artworkMapper)
            .asFlow()
            .mapToList(Dispatchers.IO)
    }

    private val table
        get() = database.favoriteArtworksQueries
}
