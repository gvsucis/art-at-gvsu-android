package edu.gvsu.art.gallery.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import edu.gvsu.art.client.Artist
import edu.gvsu.art.client.Artwork
import edu.gvsu.art.client.repository.ArtistRepository
import edu.gvsu.art.client.repository.ArtworkSearchRepository
import edu.gvsu.art.gallery.R
import edu.gvsu.art.gallery.lib.Async
import kotlinx.coroutines.*

@ExperimentalComposeUiApi
@Composable
fun SearchIndexModelList(
    selectedModel: SearchModel,
    query: String,
    onArtworkSelect: (artwork: Artwork) -> Unit = {},
    onArtistSelect: (artist: Artist) -> Unit = {},
) {
    when (selectedModel) {
        SearchModel.ARTIST -> ArtistSearchView(query) { artist ->
            onArtistSelect(artist)
        }
        SearchModel.ARTWORK -> ArtworkSearchView(query) { artwork ->
            onArtworkSelect(artwork)
        }
    }
}

@ExperimentalComposeUiApi
@Composable
fun ArtistSearchView(query: String, onClick: (Artist) -> Unit) {
    when (val data = useArtistSearch(query = query)) {
        is Async.Success -> SearchLoadedView(data(), key = { it.id }) { artist ->
            Row(
                modifier = Modifier
                    .clickable { onClick(artist) }
            ) {
                Text(
                    artist.name,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                )
            }
        }
        is Async.Loading -> LoadingView(progressIndicatorDelay = 1000L)
        else -> Unit
    }
}

@ExperimentalComposeUiApi
@Composable
fun ArtworkSearchView(query: String, onClick: (Artwork) -> Unit) {
    when (val data = useArtworkSearch(query = query)) {
        is Async.Success -> SearchLoadedView(data(), key = { it.id }) { artwork ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick(artwork) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                ArtworkRow(artwork = artwork)
            }
        }
        is Async.Loading -> LoadingView()
        else -> Unit
    }
}

@ExperimentalComposeUiApi
@Composable
fun <T> SearchLoadedView(
    list: List<T>,
    key: ((item: T) -> Any),
    itemContent: @Composable ((item: T) -> Unit),
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    if (list.isNotEmpty()) {
        val listState = rememberLazyListState()
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            items(list, key = key) {
                itemContent(it)
            }
        }
        LaunchedEffect(listState.isScrollInProgress) {
            if (listState.isScrollInProgress) {
                keyboardController?.hide()
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No results found")
        }
    }
}

enum class SearchModel {
    ARTIST,
    ARTWORK,
}

// https://usehooks.com/useDebounce/
@Composable
fun <T> useDebounce(value: T, delay: Long): T {
    val (debounced, setDebounced) = remember { mutableStateOf(value) }

    LaunchedEffect(value, delay) {
        delay(delay)
        setDebounced(value)
    }

    return debounced
}

@Composable
private fun useArtistSearch(query: String) = useSearch(query = query, fetch = {
    get<ArtistRepository>().search(it)
})

@Composable
private fun useArtworkSearch(query: String) = useSearch(query = query, fetch = {
    get<ArtworkSearchRepository>().search(query)
})

@Composable
private fun <T> useSearch(query: String, fetch: suspend (String) -> Result<T>): Async<T> {
    val debouncedQuery = useDebounce(value = query, delay = 300L)
    val state = produceState<Async<T>>(initialValue = Async.Uninitialized, debouncedQuery) {
        if (query.isBlank()) {
            value = Async.Uninitialized
            return@produceState
        }
        value = Async.Loading
        withContext(Dispatchers.IO) {
            value = fetch(query).fold(
                onSuccess = { Async.Success(it) },
                onFailure = { Async.Failure(it) }
            )
        }
    }
    return state.value
}

@Composable
fun SearchModel.localized(): String {
    return when (this) {
        SearchModel.ARTIST -> stringResource(R.string.search_index_artist_radio)
        SearchModel.ARTWORK -> stringResource(R.string.search_index_artworks_radio)
    }
}
