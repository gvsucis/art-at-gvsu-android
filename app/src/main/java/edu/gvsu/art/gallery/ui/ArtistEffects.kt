package edu.gvsu.art.gallery.ui

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import edu.gvsu.art.client.Artist
import edu.gvsu.art.client.repository.ArtistRepository
import edu.gvsu.art.gallery.lib.Async
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun useArtist(id: String): Async<Artist> {
    val repository = get<ArtistRepository>()
    val state = produceState<Async<Artist>>(initialValue = Async.Uninitialized) {
        withContext(Dispatchers.IO) {
            value = repository.find(id).fold(
                onSuccess = { Async.Success(it) },
                onFailure = { Async.Failure(it) }
            )
        }
    }

    return state.value
}
