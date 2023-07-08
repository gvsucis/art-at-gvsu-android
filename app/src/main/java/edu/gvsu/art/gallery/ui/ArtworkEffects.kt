package edu.gvsu.art.gallery.ui

import androidx.compose.runtime.*
import edu.gvsu.art.client.Artwork
import edu.gvsu.art.client.repository.ArtworkRepository
import edu.gvsu.art.client.repository.ArtworkSearchRepository
import edu.gvsu.art.client.repository.FavoritesRepository
import edu.gvsu.art.gallery.lib.Async
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun useArtwork(id: String) = useKeyedRepositoryResource(fetch = {
    get<ArtworkRepository>().find(id)
})

@Composable
fun useFavorite(artworkID: String): Pair<Boolean, () -> Unit> {
    val repository = get<FavoritesRepository>()
    val (isFavorite, setFavorite) = remember { mutableStateOf(false) }

    LaunchedEffect(artworkID) {
        withContext(Dispatchers.IO) {
            setFavorite(repository.exists(artworkID))
        }
    }

    return Pair(isFavorite, { setFavorite(repository.toggle(artworkID)) })
}

@Composable
fun useFeaturedArtworks(): Async<List<Artwork>> {
    val state = produceState<Async<List<Artwork>>>(initialValue = Async.Uninitialized) {
        withContext(Dispatchers.IO) {
            value = get<ArtworkSearchRepository>().featured().fold(
                onSuccess = { Async.Success(it) },
                onFailure = { Async.Failure(it) }
            )
        }
    }
    return state.value
}

@Composable
fun useRandomFeaturedArtwork(): Async<Artwork> {
    val state = produceState<Async<Artwork>>(initialValue = Async.Uninitialized) {
        withContext(Dispatchers.IO) {
            value =  get<ArtworkSearchRepository>().featured().fold(
                onSuccess = { Async.Success(it.random()) },
                onFailure = { Async.Failure(it) }
            )
        }
    }
    return state.value
}

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


