package edu.gvsu.art.gallery.ui.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults.pinnedScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import edu.gvsu.art.client.api.visionsearch.ImageResult
import edu.gvsu.art.gallery.R
import edu.gvsu.art.gallery.extensions.nestedScaffoldPadding
import edu.gvsu.art.gallery.lib.Async
import edu.gvsu.art.gallery.ui.ErrorView
import edu.gvsu.art.gallery.ui.GalleryTopAppBar
import edu.gvsu.art.gallery.ui.LoadingView
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisionSearchResultsScreen(
    viewModel: VisionSearchResultsViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToArtwork: (artworkID: String) -> Unit,
) {
    val results = viewModel.similarWorks.collectAsStateWithLifecycle()
    val data = results.value
    val scrollBehavior = pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            GalleryTopAppBar(
                title = stringResource(R.string.vision_search_results_title),
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
    ) { padding ->
        Box(Modifier.nestedScaffoldPadding(padding)) {
            AsyncImage(
                model = viewModel.uri,
                contentDescription = "Captured image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
//            when (data) {
//                is Async.Success -> ResultList(onClick = onNavigateToArtwork, artworks = data())
//                is Async.Failure -> ErrorView(
//                    error = data.error,
//                    onRetryClick = { viewModel.retry() }
//                )
//
//                else -> LoadingView()
//            }
        }
    }
}

@Composable
fun ResultList(onClick: (artworkID: String) -> Unit, artworks: List<ImageResult>) {
    LazyVerticalStaggeredGrid(
        modifier = Modifier.fillMaxSize(),
        columns = StaggeredGridCells.Adaptive(200.dp),
    ) {
        items(artworks) { item ->
            Box(
                Modifier.clickable {
                    onClick(item.id)
                }
            ) {
                AsyncImage(
                    model = item.imageURL,
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .clip(RoundedCornerShape(10.dp))
                )
            }
        }
    }
}
