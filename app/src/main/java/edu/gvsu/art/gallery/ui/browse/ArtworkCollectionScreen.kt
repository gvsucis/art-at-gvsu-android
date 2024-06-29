package edu.gvsu.art.gallery.ui.browse

import androidx.compose.runtime.Composable
import org.koin.androidx.compose.koinViewModel

@Composable
fun ArtworkCollectionScreen(
    viewModel: ArtworkCollectionViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToArtwork: (artworkID: String) -> Unit,
) {
    ArtworkCollectionView(
        onNavigateBack = onNavigateBack,
        onNavigateToArtwork = onNavigateToArtwork,
        collection = viewModel.collection,
        artworks = viewModel.artworks
    )
}
