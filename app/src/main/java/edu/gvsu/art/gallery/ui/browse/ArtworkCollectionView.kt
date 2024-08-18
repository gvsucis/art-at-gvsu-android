package edu.gvsu.art.gallery.ui.browse

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.TopAppBarDefaults.pinnedScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import edu.gvsu.art.client.Artwork
import edu.gvsu.art.client.ArtworkCollection
import edu.gvsu.art.gallery.R
import edu.gvsu.art.gallery.ui.ArtworkRow
import edu.gvsu.art.gallery.ui.GalleryTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtworkCollectionView(
    onNavigateBack: () -> Unit,
    onNavigateToArtwork: (artworkID: String) -> Unit,
    collection: ArtworkCollection,
    artworks: List<Artwork>,
) {
    val scrollBehavior = pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .fillMaxSize(),
        topBar = {
            GalleryTopAppBar(
                title = title(collection),
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            LazyColumn {
                items(artworks, key = { it.id }) { artwork ->
                    ArtworkRow(
                        artwork = artwork,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToArtwork(artwork.id) }
                            .padding(PaddingValues(horizontal = 16.dp, vertical = 8.dp))
                    )
                }
            }
        }
    }
}

@Composable
fun title(collection: ArtworkCollection): String {
    return when (collection) {
        ArtworkCollection.FeaturedArt -> stringResource(R.string.navigation_collection_featured)
        ArtworkCollection.FeaturedAR -> stringResource(R.string.navigation_collection_augmented_reality)
    }
}
