package edu.gvsu.art.client.repository

import edu.gvsu.art.client.Artwork
import edu.gvsu.art.db.ArtGalleryDatabase

interface FavoritesRepository {
    fun exists(artworkID: String): Boolean
    fun toggle(artworkID: String): Boolean
    fun all(): List<Artwork>
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

    override fun all(): List<Artwork> {
        return table
            .findAll()
            .executeAsList()
            .map { it.toDomainModel }
    }

    private val table
        get() = database.favoriteArtworksQueries
}
