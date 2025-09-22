package edu.gvsu.art.gallery.ui.artwork.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import edu.gvsu.art.client.Artwork
import edu.gvsu.art.client.ArtworkCollection
import edu.gvsu.art.client.repository.ArtworkSearchRepository
import edu.gvsu.art.gallery.lib.Async
import edu.gvsu.art.gallery.lib.FileDownloader
import edu.gvsu.art.gallery.lib.arAssetsDirectory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

// https://github.com/gvsucis/art-at-gvsu-android-private/pull/120/files
class ArtworkARViewModel(
    private val repository: ArtworkSearchRepository,
    application: Application
) : AndroidViewModel(application) {
    val collection = ArtworkCollection.FeaturedAR
    val artworks = MutableStateFlow<Async<List<Artwork>>>(Async.Uninitialized)
    
    var artworkVideos: Map<String, URL> = emptyMap()
        private set

    init {
        fetchArtworks()
    }

    private suspend fun downloadImage(artwork: Artwork) {
        FileDownloader.download(
            url = artwork.arDigitalAssetURL!!.toString(),
            directory = application.applicationContext.arAssetsDirectory()
        ).onSuccess {
        }
    }

    private fun fetchArtworks() {
        artworks.value = Async.Loading

        viewModelScope.launch(Dispatchers.IO) {
            val value = repository.searchCollection(collection)

            withContext(Dispatchers.Main) {
                artworks.value = value.fold(
                    onSuccess = { result ->
                        artworkVideos = result.mapNotNull { artwork ->
                            artwork.arDigitalAssetURL?.let { url ->
                                artwork.id to url
                            }
                        }.toMap()
                        Async.Success(result)
                    },
                    onFailure = { Async.Failure(it) }
                )
            }
        }
    }
}
