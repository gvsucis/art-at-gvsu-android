package edu.gvsu.art.gallery.ui.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.gvsu.art.client.Artist
import edu.gvsu.art.client.Artwork
import edu.gvsu.art.client.repository.ArtistRepository
import edu.gvsu.art.client.repository.ArtworkSearchRepository
import edu.gvsu.art.gallery.lib.Async
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SearchViewModel(
    private val artworkRepository: ArtworkSearchRepository,
    private val artistRepository: ArtistRepository,
) : ViewModel() {
    var query by mutableStateOf("")
        private set

    var searchState by mutableStateOf<Async<UnifiedSearchResults>>(Async.Uninitialized)
        private set

    private var searchJob: Job? = null

    fun updateQuery(query: String) {
        this.query = query
        debounceSearch()
    }

    private fun debounceSearch() {
        searchJob?.cancel()

        if (query.isBlank()) {
            searchState = Async.Uninitialized
            return
        }

        searchJob = viewModelScope.launch {
            delay(DEBOUNCE_DELAY_MS)
            performSearch()
        }
    }

    private suspend fun performSearch() {
        searchState = Async.Loading

        withContext(Dispatchers.IO) {
            val artworksDeferred = async { artworkRepository.search(query, limit = PREVIEW_LIMIT + 1) }
            val artistsDeferred = async { artistRepository.search(query, limit = PREVIEW_LIMIT + 1) }

            val artworks = artworksDeferred.await().getOrDefault(emptyList())
            val artists = artistsDeferred.await().getOrDefault(emptyList())

            withContext(Dispatchers.Main) {
                searchState = Async.Success(
                    UnifiedSearchResults(
                        artworks = artworks,
                        artists = artists,
                    )
                )
            }
        }
    }

    companion object {
        private const val DEBOUNCE_DELAY_MS = 300L
        private const val PREVIEW_LIMIT = 10
    }
}

data class UnifiedSearchResults(
    val artworks: List<Artwork> = emptyList(),
    val artists: List<Artist> = emptyList(),
) {
    val artworksPreview: List<Artwork>
        get() = artworks.take(PREVIEW_LIMIT)

    val artistsPreview: List<Artist>
        get() = artists.take(PREVIEW_LIMIT)

    val hasMoreArtworks: Boolean
        get() = artworks.size > PREVIEW_LIMIT

    val hasMoreArtists: Boolean
        get() = artists.size > PREVIEW_LIMIT

    companion object {
        private const val PREVIEW_LIMIT = 10
    }
}
