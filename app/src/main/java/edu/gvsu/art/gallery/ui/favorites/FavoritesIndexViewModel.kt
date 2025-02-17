package edu.gvsu.art.gallery.ui.favorites

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import edu.gvsu.art.client.repository.FavoritesRepository
import edu.gvsu.art.gallery.bookmarks.BookmarksImportWorker

class FavoritesIndexViewModel(
    repository: FavoritesRepository,
    application: Application,
) : AndroidViewModel(application) {
    val favorites = repository.all()

    fun startImport(uri: Uri?) {
        uri ?: return

        BookmarksImportWorker.performAsync(context, uri)
    }

    private val context: Context
        get() = getApplication<Application>().applicationContext

}
