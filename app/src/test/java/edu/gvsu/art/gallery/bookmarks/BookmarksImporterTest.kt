package edu.gvsu.art.gallery.bookmarks

import edu.gvsu.art.client.api.ObjectDetail
import edu.gvsu.art.client.repository.ArtworkRepository
import edu.gvsu.art.client.repository.DefaultArtworkRepository
import edu.gvsu.art.client.repository.DefaultFavoritesRepository
import edu.gvsu.art.client.repository.FavoritesRepository
import edu.gvsu.art.db.ArtGalleryDatabase
import edu.gvsu.art.gallery.InMemoryDatabase
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.math.exp
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BookmarksImporterTest {
    private lateinit var database: ArtGalleryDatabase
    private lateinit var favoritesRecords: FavoritesRepository
    private lateinit var artworks: ArtworkRepository
    private lateinit var importer: BookmarksImporter

    private val objectDetails = listOf(
        ObjectDetail(object_id = 3881, object_name = "GVSU Marching Band"),
        ObjectDetail(object_id = 6836, object_name = "I'm Halfway There..."),
        ObjectDetail(object_id = 9509, object_name = "Pulling Out The Boat"),
    )

    @BeforeTest
    fun setup() {
        database = InMemoryDatabase()
        favoritesRecords = DefaultFavoritesRepository(database)
        artworks = DefaultArtworkRepository(database, client = mockk())

        objectDetails.forEach {
            artworks.insert(it)
        }

        importer = BookmarksImporter(favoritesRecords, artworks)
    }

    @Test
    fun `it imports favorites from HTML`() = runTest {
        val html = testFile("favorites.html").inputStream()
        importer.import(html)

        val favorites = favoritesRecords.all().first()

        assertEquals(expected = 3, actual = favorites.size)
    }
}

fun testFile(resource: String): File {
    return File(testResource(resource))
}

fun testResource(resource: String): String {
    return "src/test/resources/${resource}"
}
