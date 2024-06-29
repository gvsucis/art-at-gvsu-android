package edu.gvsu.art.gallery.ui.browse

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import edu.gvsu.art.client.Artwork
import edu.gvsu.art.client.ArtworkCollection
import edu.gvsu.art.gallery.R
import edu.gvsu.art.gallery.ui.ArtworkRow
import edu.gvsu.art.gallery.ui.GalleryTopAppBar

@Composable
fun ArtworkCollectionView(
    onNavigateBack: () -> Unit,
    onNavigateToArtwork: (artworkID: String) -> Unit,
    collection: ArtworkCollection,
    artworks: List<Artwork>,
) {
    Scaffold(
        topBar = {
            GalleryTopAppBar(
                title = title(collection),
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            LazyColumn {
                itemsIndexed(artworks, key = { _, artwork -> artwork.id }) { index, artwork ->
                    ArtworkRow(
                        artwork = artwork,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToArtwork(artwork.id) }
                            .padding(PaddingValues(horizontal = 16.dp, vertical = 8.dp))
                    )
                    if (index != artworks.lastIndex) {
                        Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun title(collection: ArtworkCollection): String {
    return when(collection) {
        ArtworkCollection.FeaturedArt -> stringResource(R.string.navigation_collection_featured)
        ArtworkCollection.AR -> stringResource(R.string.navigation_collection_augmented_reality)
    }
}
