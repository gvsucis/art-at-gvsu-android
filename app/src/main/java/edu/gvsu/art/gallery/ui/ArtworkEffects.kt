package edu.gvsu.art.gallery.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import edu.gvsu.art.client.Artwork
import edu.gvsu.art.client.ArtworkCollection
import edu.gvsu.art.client.repository.ArtworkSearchRepository
import edu.gvsu.art.client.repository.FavoritesRepository
import edu.gvsu.art.gallery.lib.Async
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun useFavorites(): Async<List<Artwork>> {
    val repository = get<FavoritesRepository>()
    val state = produceState<Async<List<Artwork>>>(initialValue = Async.Uninitialized) {
        withContext(Dispatchers.IO) {
            value = Async.Success(repository.all())
        }
    }
    return state.value
}
