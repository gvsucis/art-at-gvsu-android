package edu.gvsu.art.gallery.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults.pinnedScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import edu.gvsu.art.client.Artwork
import edu.gvsu.art.client.Location
import edu.gvsu.art.gallery.R
import edu.gvsu.art.gallery.extensions.nestedScaffoldPadding
import edu.gvsu.art.gallery.lib.Async
import edu.gvsu.art.gallery.navigateToArtworkDetail
import edu.gvsu.art.gallery.navigateToLocation
import edu.gvsu.art.gallery.ui.foundation.LocalTopLevelRoute
import edu.gvsu.art.gallery.ui.theme.ArtGalleryTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationDetailScreen(navController: NavController, locationID: String?, locationName: String) {
    locationID ?: return

    val (data, retry) = useLocation(locationID)
    val scrollBehavior = pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            GalleryTopAppBar(
                title = locationName,
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LocationList(navController: NavController, location: Location) {
    val tabScreen = LocalTopLevelRoute.current

    if (location.locations.isEmpty() && location.distinctWorks.isEmpty()) {
        LocationEmptyView()
    } else {
        LazyColumn {
            childLocations(locations = location.locations) { selected ->
                navController.navigateToLocation(selected.id, selected.name)
            }
            artworks(artworks = location.distinctWorks) { selected ->
                navController.navigateToArtworkDetail(tabScreen, selected.id)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.childLocations(
    locations: List<Location>,
    navigateToLocation: (location: Location) -> Unit,
) {
    if (locations.isEmpty()) {
        return
    }

    stickyHeader {
        SectionHeader(stringResource(R.string.location_detail_child_locations))
    }

    items(locations, key = { "location:${it.id}" }) { location ->
        Box(
            modifier = Modifier
                .clickable { navigateToLocation(location) }
        ) {
            Text(
                location.name, modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}

@ExperimentalFoundationApi
fun LazyListScope.artworks(
    artworks: List<Artwork>,
    navigateToArtwork: (artwork: Artwork) -> Unit,
) {
    if (artworks.isEmpty()) {
        return
    }

    stickyHeader {
        SectionHeader(stringResource(R.string.location_detail_artworks))
    }

    items(artworks, key = { item -> "artwork:${item.id}" }) { artwork ->
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
private fun SectionHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = colorScheme.surfaceContainerHigh)
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            style = typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = colorScheme.onSurface,
        )
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

@Preview
@Composable
private fun SectionHeaderPreview() {
    ArtGalleryTheme {
        SectionHeader(title = "Title goes here")
    }
}

@Composable
@Preview
fun PreviewEmptyView() {
    ArtGalleryTheme {
        LocationEmptyView()
    }
}
