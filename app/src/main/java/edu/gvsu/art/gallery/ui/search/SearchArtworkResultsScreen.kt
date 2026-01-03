package edu.gvsu.art.gallery.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import edu.gvsu.art.client.Artwork
import edu.gvsu.art.client.repository.ArtworkSearchRepository
import edu.gvsu.art.gallery.R
import edu.gvsu.art.gallery.extensions.nestedScaffoldPadding
import edu.gvsu.art.gallery.lib.Async
import edu.gvsu.art.gallery.ui.ArtworkRow
import edu.gvsu.art.gallery.ui.GalleryTopAppBar
import edu.gvsu.art.gallery.ui.LoadingView
import edu.gvsu.art.gallery.ui.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchArtworkResultsScreen(
    query: String,
    onNavigateBack: () -> Unit,
    onNavigateToArtwork: (artworkID: String) -> Unit,
) {
    val artworkRepository = get<ArtworkSearchRepository>()
    var searchState by remember { mutableStateOf<Async<List<Artwork>>>(Async.Loading) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    LaunchedEffect(query) {
        withContext(Dispatchers.IO) {
            val result = artworkRepository.search(query)
            searchState = result.fold(
                onSuccess = { Async.Success(it) },
                onFailure = { Async.Failure(it) }
            )
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            GalleryTopAppBar(
                title = stringResource(R.string.search_index_artworks_radio),
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .nestedScaffoldPadding(padding)
        ) {
            when (searchState) {
                is Async.Loading -> LoadingView()
                is Async.Success -> {
                    val artworks = (searchState as Async.Success<List<Artwork>>)()
                    if (artworks.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(stringResource(R.string.search_list_no_results_found))
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(artworks, key = { it.id }) { artwork ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onNavigateToArtwork(artwork.id) }
                                        .padding(16.dp)
                                ) {
                                    ArtworkRow(artwork = artwork)
                                }
                            }
                        }
                    }
                }
                else -> Unit
            }
        }
    }
}
