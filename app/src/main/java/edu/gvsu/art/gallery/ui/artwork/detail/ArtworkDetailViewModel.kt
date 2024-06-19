package edu.gvsu.art.gallery.ui.artwork.detail

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.gvsu.art.client.Artwork
import edu.gvsu.art.client.repository.ArtworkRepository
import edu.gvsu.art.client.repository.FavoritesRepository
import edu.gvsu.art.gallery.ArtworkDetailArgs
import edu.gvsu.art.gallery.lib.Async
import edu.gvsu.art.gallery.lib.FileDownloader
import edu.gvsu.art.gallery.lib.arAssetsDirectory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ArtworkDetailViewModel(
    handle: SavedStateHandle,
    private val artworks: ArtworkRepository,
    private val favorites: FavoritesRepository,
) : ViewModel() {
    private val _artwork = mutableStateOf<Async<Artwork>>(Async.Uninitialized)
    private val _favorite = mutableStateOf(false)
    private val args = ArtworkDetailArgs(handle)

    init {
        fetchArtwork()
        checkFavorite()
    }

    val artwork: Artwork?
        get() = when (_artwork.value) {
            is Async.Success -> _artwork.value.invoke()
            else -> null
        }

    fun toggleFavorite() {
        _favorite.value = favorites.toggle(args.artworkID)
    }

    val isFavorite: Boolean
        get() = _favorite.value

    private fun fetchArtwork() {
        _artwork.value = Async.Loading

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val value = artworks.find(args.artworkID)

                withContext(Dispatchers.Main) {
                    _artwork.value = value.fold(
                        onSuccess = { Async.Success(it) },
                        onFailure = { Async.Failure(it) }
                    )
                }
            }
        }
    }

    private fun checkFavorite() {
        _favorite.value = favorites.exists(args.artworkID)
    }
}
