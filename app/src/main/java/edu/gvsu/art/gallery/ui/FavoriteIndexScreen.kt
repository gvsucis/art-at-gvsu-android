package edu.gvsu.art.gallery.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import edu.gvsu.art.client.Artwork
import edu.gvsu.art.gallery.R
import edu.gvsu.art.gallery.Route
import edu.gvsu.art.gallery.TabScreen
import edu.gvsu.art.gallery.lib.Async
import edu.gvsu.art.gallery.navigateToArtworkDetail

@Composable
fun FavoriteIndexScreen(navController: NavController) {
    val context = LocalContext.current
    val data = useFavorites()

    fun navigateToArtwork(artworkID: String) {
        navController.navigateToArtworkDetail(TabScreen.Favorites, artworkID = artworkID)
    }

    fun navigateToBrowse() {
        navController.navigate(Route.BrowseIndex)
    }

    fun shareFavorites() {
        data()?.let { favorites ->
            context.shareFavoritesHTML(favorites)
        }
    }

    Scaffold(
        topBar = {
            GalleryTopAppBar(
                title = stringResource(id = R.string.navigation_Favorites),
                actions = {
                    IconButton(onClick = { shareFavorites() }) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = null,
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (data is Async.Success) {
                FavoritesLoadedView(
                    favorites = data(),
                    onArtworkClick = { navigateToArtwork(it) },
                    navigateToBrowse = { navigateToBrowse() }
                )
            }
        }
    }
}

@Composable
private fun FavoritesLoadedView(
    favorites: List<Artwork>,
    onArtworkClick: (artworkID: String) -> Unit,
    navigateToBrowse: () -> Unit,
) {
    if (favorites.isEmpty()) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(stringResource(R.string.favorites_index_favorites_will_display_here))
            Text(stringResource(R.string.favorites_index_how_to_favorite))
            Button(
                onClick = { navigateToBrowse() },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(stringResource(R.string.favorites_index_browse_artworks))
            }
        }
    }
    LazyColumn {
        items(favorites, key = { it.id }) { favorite ->
            Box(
                Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                WideTitleCard(
                    title = favorite.name,
                    subtitle = favorite.formattedArtistName,
                    imageURL = favorite.mediaLarge,
                    onClick = { onArtworkClick(favorite.id) }
                )
            }
        }
    }
}
