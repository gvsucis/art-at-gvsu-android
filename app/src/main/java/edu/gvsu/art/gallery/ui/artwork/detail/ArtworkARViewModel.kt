package edu.gvsu.art.gallery.ui.artwork.detail

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import edu.gvsu.art.client.Artwork
import edu.gvsu.art.client.ArtworkCollection
import edu.gvsu.art.client.repository.ArtworkSearchRepository
import edu.gvsu.art.gallery.ArtworkCollectionArgs
import edu.gvsu.art.gallery.lib.Async
import edu.gvsu.art.gallery.lib.FileDownloader
import edu.gvsu.art.gallery.lib.arAssetsDirectory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// https://github.com/gvsucis/art-at-gvsu-android-private/pull/120/files
class ArtworkARViewModel(
    private val repository: ArtworkSearchRepository,
    application: Application
) : AndroidViewModel(application) {
    val collection = ArtworkCollection.FeaturedAR
    private val _artworks = mutableStateOf<Async<List<Artwork>>>(Async.Uninitialized)

    init {
        fetchArtworks()
    }

    var configurationListener: ((artworks: List<Artwork>) -> Unit)? = null

    var recognitionListener: ()

    private suspend fun something(artwork: Artwork) {
        FileDownloader.download(
            url = artwork.arDigitalAssetURL!!.toString(),
            directory = application.applicationContext.arAssetsDirectory()
        ).onSuccess {

        }
    }

    private fun fetchArtworks() {
        _artworks.value = Async.Loading

        viewModelScope.launch(Dispatchers.IO) {
            val value = repository.searchCollection(collection)

            withContext(Dispatchers.Main) {
                _artworks.value = value.fold(
                    onSuccess = { Async.Success(it) },
                    onFailure = { Async.Failure(it) }
                )
            }
        }
    }
}
