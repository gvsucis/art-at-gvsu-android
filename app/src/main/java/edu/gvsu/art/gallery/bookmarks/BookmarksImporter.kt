package edu.gvsu.art.gallery.bookmarks

import edu.gvsu.art.client.repository.ArtworkRepository
import edu.gvsu.art.client.repository.FavoritesRepository
import org.jsoup.Jsoup
import java.io.InputStream

class BookmarksImporter(
    val favorites: FavoritesRepository,
    val artworks: ArtworkRepository,
) {
    suspend fun import(
        inputStream: InputStream,
        onProgress: (progress: ImportProgress) -> Unit = {},
    ) {
        var counter = 0
        val document = Jsoup.parse(inputStream, null, "")
        val links = document.select("a")
        val size = links.size

        onProgress(ImportProgress(currentCount = 0, total = size))

        links.forEach { link ->
            val id = parseID(link.attr("href"))

            if (id != null) {
                artworks.find(id).onSuccess { artwork ->
                    favorites.add(artwork.id)
                }
            }

            counter += 1
            onProgress(
                ImportProgress(
                    currentCount = counter,
                    total = size,
                )
            )
        }
    }
}

private fun parseID(url: String): String? {
    return url.split("/").lastOrNull()
}

data class ImportProgress(
    val currentCount: Int = 0,
    val total: Int = 0,
) {
    val percent: Float
        get() = (currentCount.toFloat() / total.toFloat()).coerceIn(0f, 1f)
}
