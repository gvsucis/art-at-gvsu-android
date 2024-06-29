package edu.gvsu.art.gallery.ui.browse

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.gvsu.art.client.Artwork
import edu.gvsu.art.client.repository.ArtworkSearchRepository
import edu.gvsu.art.gallery.ArtworkCollectionArgs
import edu.gvsu.art.gallery.lib.Async
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ArtworkCollectionViewModel(
    handle: SavedStateHandle,
    private val repository: ArtworkSearchRepository,
) : ViewModel() {
    private val args = ArtworkCollectionArgs(handle)
    private val _artworks = mutableStateOf<Async<List<Artwork>>>(Async.Uninitialized)

    val collection = args.collection

    init {
        fetchArtworks()
    }

    val artworks: List<Artwork>
        get() = _artworks.value.invoke().orEmpty()

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
