package edu.gvsu.art.gallery.ui.browse

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.gvsu.art.client.Artwork
import edu.gvsu.art.client.ArtworkCollection
import edu.gvsu.art.client.repository.ArtworkSearchRepository
import edu.gvsu.art.gallery.lib.Async
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BrowseIndexViewModel(
    private val repository: ArtworkSearchRepository,
) : ViewModel() {
    private val _artwork = mutableStateOf<Async<Artwork>>(Async.Uninitialized)

    init {
        fetchFeaturedArtwork()
    }

    val artwork: Artwork
        get() = _artwork.value.invoke() ?: Artwork()

    private fun fetchFeaturedArtwork() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.searchCollection(ArtworkCollection.FeaturedArt).fold(
                onSuccess = { Async.Success(it.random()) },
                onFailure = { Async.Failure(it) }
            )

            withContext(Dispatchers.Main) {
                _artwork.value = result
            }
        }
    }
}
