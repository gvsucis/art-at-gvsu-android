package edu.gvsu.art.gallery.ui

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import edu.gvsu.art.gallery.R
import edu.gvsu.art.gallery.Route
import edu.gvsu.art.gallery.lib.Async
import edu.gvsu.art.gallery.navigateToArtworkDetail
import edu.gvsu.art.gallery.ui.foundation.LocalTabScreen

@Composable
fun FeaturedArtIndexScreen(navController: NavController) {
    val currentTab = LocalTabScreen.current
    val artworks = when (val data = useFeaturedArtworks()) {
        is Async.Success -> data()
        else -> listOf()
    }

    fun navigateToArtwork(id: String) {
        navController.navigateToArtworkDetail(currentTab, id)
    }

    Column {
        GalleryTopAppBar(
            title = stringResource(R.string.navigation_featured_index),
            navigationIcon = {
                IconButton(onClick = { navController.navigate(Route.BrowseIndex) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                }
            }
        )
        LazyColumn {
            itemsIndexed(artworks, key = { _, artwork -> artwork.id }) { index, artwork ->
                ArtworkRow(
                    artwork = artwork,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navigateToArtwork(artwork.id) }
                        .padding(PaddingValues(horizontal = 16.dp, vertical = 8.dp))
                )
                if (index != artworks.lastIndex) {
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}
