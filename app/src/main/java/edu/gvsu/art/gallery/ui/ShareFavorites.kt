package edu.gvsu.art.gallery.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import edu.gvsu.art.client.Artwork
import edu.gvsu.art.gallery.BuildConfig
import edu.gvsu.art.gallery.R
import edu.gvsu.art.gallery.lib.BookmarksExport
import edu.gvsu.art.gallery.lib.Links
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun Context.shareFavoritesHTML(favorites: List<Artwork>) {
    val formattedTime =
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    val bookmarks = BookmarksExport.Builder().run {
        favorites.forEach { favorite ->
            addBookmark(favorite.name, Links.artworkDetail(favorite.id))
        }
        build()
    }

    val uri = writeBookmark(bookmarks.html)
    if (uri != null) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = contentResolver.getType(uri)
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(
            Intent.createChooser(
                shareIntent,
                getString(R.string.favorites_index_export_chooser_title)
            )
        )
    }
}

fun Context.writeBookmark(str: String): Uri? {
    val filename = "art_at_gvsu_favorites.html"
    val bookmarks = File(cacheDir, "bookmarks").apply {
        if (!exists()) {
            mkdir()
        }
    }

    val file = File(bookmarks, filename)

    try {
        file.createNewFile()
        FileOutputStream(file).apply {
            write(str.toByteArray())
            close()
        }
    } catch (e: IOException) {
        return null
    }

    return try {
        fileURI(file)
    } catch (e: IllegalArgumentException) {
        null
    }
}

fun Context.fileURI(file: File): Uri =
    FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.fileprovider", file)
