package edu.gvsu.art.gallery.ui

import android.content.Context
import android.net.Uri
import android.widget.Toast
import edu.gvsu.art.client.Artwork
import edu.gvsu.art.gallery.R
import edu.gvsu.art.gallery.bookmarks.BookmarksExport
import edu.gvsu.art.gallery.lib.Links
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileOutputStream

suspend fun Context.exportFavorites(favorites: List<Artwork>, target: Uri?) {
    target ?: return

    val result = runCatching {
        val (html) = BookmarksExport.Builder().run {
            favorites.forEach { favorite ->
                addBookmark(favorite.name, Links.artworkDetail(favorite.id))
            }
            build()
        }

        return@runCatching withContext(Dispatchers.IO) {
            contentResolver.openFileDescriptor(target, "w")?.use { descriptor ->
                FileOutputStream(descriptor.fileDescriptor).use {
                    it.write(html.toByteArray())
                }
            }
        }
    }

    val messageRes = result.fold(
        onSuccess = { R.string.favorites_export_success },
        onFailure = { R.string.favorites_export_failure }
    )

    Toast.makeText(this, getString(messageRes), Toast.LENGTH_SHORT).show()
}
