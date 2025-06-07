package edu.gvsu.art.gallery.ui.search

import androidx.compose.runtime.Composable
import coil.compose.AsyncImage
import org.koin.androidx.compose.koinViewModel

@Composable
fun VisionSearchResultsScreen(
    viewModel: VisionSearchResultsViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToArtwork: (artworkID: String) -> Unit,
) {
    AsyncImage(viewModel.uri, contentDescription = null)
}
