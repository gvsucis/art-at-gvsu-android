package edu.gvsu.art.gallery.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import edu.gvsu.art.client.Artwork
import edu.gvsu.art.client.Location
import edu.gvsu.art.gallery.R
import edu.gvsu.art.gallery.lib.Async
import edu.gvsu.art.gallery.navigateToArtworkDetail
import edu.gvsu.art.gallery.navigateToLocation
import edu.gvsu.art.gallery.ui.foundation.LocalTabScreen
import edu.gvsu.art.gallery.ui.theme.ArtAtGVSUTheme

@Composable
fun LocationDetailScreen(navController: NavController, locationID: String?, locationName: String) {
    locationID ?: return

    val (data, retry) = useLocation(locationID)

    Column {
        GalleryTopAppBar(
            title = locationName,
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                }
            }
        )
        when (data) {
            is Async.Success ->
                LocationList(navController, location = data())
            is Async.Failure ->
                ErrorView(
                    error = data.error,
                    onRetryClick = { retry() }
                )
            else -> LoadingView()
        }
    }
}

@Composable
private fun LocationList(navController: NavController, location: Location) {
    val tabScreen = LocalTabScreen.current

    if (location.locations.isEmpty() && location.artworks.isEmpty()) {
        return LocationEmptyView()
    }

    LazyColumn {
        childLocations(locations = location.locations) { selected ->
            navController.navigateToLocation(selected.id, selected.name)
        }
        artworks(artworks = location.artworks) { selected ->
            navController.navigateToArtworkDetail(tabScreen, selected.id)
        }
    }
}

private fun LazyListScope.childLocations(
    locations: List<Location>,
    navigateToLocation: (location: Location) -> Unit,
) {
    if (locations.isEmpty()) {
        return
    }

    item("location_section_title") {
        SectionTitle(stringResource(R.string.location_detail_child_locations))
    }

    items(locations, key = { "location:${it.id}" }) { location ->
        Box(
            modifier = Modifier
                .clickable { navigateToLocation(location) }
        ) {
            Text(location.name, modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
            )
        }
    }
}

fun LazyListScope.artworks(
    artworks: List<Artwork>,
    navigateToArtwork: (artwork: Artwork) -> Unit,
) {
    if (artworks.isEmpty()) {
        return
    }

    item(key = "location_artwork_title") {
        SectionTitle(stringResource(R.string.location_detail_artworks))
    }

    itemsIndexed(artworks, key = { index, item -> "artwork:${item.id}_$index" }) { _, artwork ->
        ArtworkRow(
            artwork = artwork,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navigateToArtwork(artwork) }
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            title,
            style = MaterialTheme.typography.subtitle1,
            modifier = Modifier
                .alpha(0.8f)
                .padding(vertical = 8.dp)

        )
        Divider()
    }
}

@Composable
private fun LocationEmptyView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(stringResource(R.string.location_detail_empty_message))
    }
}

@Composable
@Preview
fun PreviewEmptyView() {
    ArtAtGVSUTheme {
        LocationEmptyView()
    }
}
