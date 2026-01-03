package edu.gvsu.art.gallery.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import edu.gvsu.art.client.Artist
import edu.gvsu.art.client.Artwork
import edu.gvsu.art.gallery.R
import edu.gvsu.art.gallery.lib.Async
import edu.gvsu.art.gallery.ui.LoadingView

@ExperimentalComposeUiApi
@Composable
fun SearchResultsView(
    searchState: Async<UnifiedSearchResults>,
    onArtworkSelect: (artwork: Artwork) -> Unit,
    onArtistSelect: (artist: Artist) -> Unit,
    onSeeMoreArtworks: () -> Unit,
    onSeeMoreArtists: () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    LaunchedEffect(scrollState.isScrollInProgress) {
        if (scrollState.isScrollInProgress) {
            keyboardController?.hide()
        }
    }

    when (searchState) {
        is Async.Loading -> LoadingView(progressIndicatorDelay = 500L)
        is Async.Success -> {
            val results = searchState()
            if (results.artworks.isEmpty() && results.artists.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.search_list_no_results_found))
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    if (results.artworksPreview.isNotEmpty()) {
                        SearchResultsSection(
                            title = stringResource(R.string.search_index_artworks_radio),
                            showSeeMore = results.hasMoreArtworks,
                            onSeeMore = onSeeMoreArtworks,
                        ) {
                            SearchResultsRow(
                                items = results.artworksPreview,
                                key = { it.id },
                            ) { artwork ->
                                ArtworkCard(
                                    artwork = artwork,
                                    onClick = { onArtworkSelect(artwork) }
                                )
                            }
                        }
                    }

                    if (results.artistsPreview.isNotEmpty()) {
                        SearchResultsSection(
                            title = stringResource(R.string.search_index_artist_radio),
                            showSeeMore = results.hasMoreArtists,
                            onSeeMore = onSeeMoreArtists,
                        ) {
                            SearchResultsRow(
                                items = results.artistsPreview,
                                key = { it.id },
                            ) { artist ->
                                ArtistCard(
                                    artist = artist,
                                    onClick = { onArtistSelect(artist) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        else -> Unit
    }
}

@Composable
private fun SearchResultsSection(
    title: String,
    showSeeMore: Boolean,
    onSeeMore: () -> Unit,
    content: @Composable () -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            if (showSeeMore) {
                TextButton(onClick = onSeeMore) {
                    Text(stringResource(R.string.search_see_more))
                }
            }
        }
        content()
    }
}

@Composable
private fun <T : Any> SearchResultsRow(
    items: List<T>,
    key: (T) -> Any,
    itemContent: @Composable (T) -> Unit,
) {
    LazyRow(
        modifier = Modifier.height(220.dp),
        contentPadding = PaddingValues(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(items, key = key) { item ->
            itemContent(item)
        }
    }
}

@Composable
private fun ArtworkCard(
    artwork: Artwork,
    onClick: () -> Unit,
) {
    val cornerShape = RoundedCornerShape(8.dp)

    Column(
        modifier = Modifier
            .width(140.dp)
            .clip(cornerShape)
            .clickable { onClick() }
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(cornerShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            AsyncImage(
                model = artwork.mediaMedium?.toString(),
                contentDescription = artwork.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(cornerShape)
            )
        }
        Column {
            Text(
                text = artwork.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (artwork.formattedArtistName.isNotBlank()) {
                Text(
                    text = artwork.formattedArtistName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun ArtistCard(
    artist: Artist,
    onClick: () -> Unit,
) {
    val cornerShape = RoundedCornerShape(8.dp)
    val relatedWorkThumbnail = artist.relatedWorks.firstOrNull()?.thumbnail

    Column(
        modifier = Modifier
            .width(140.dp)
            .clip(cornerShape)
            .clickable { onClick() }
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(cornerShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            if (relatedWorkThumbnail != null) {
                AsyncImage(
                    model = relatedWorkThumbnail.toString(),
                    contentDescription = artist.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(cornerShape)
                )
            } else {
                Text(
                    text = artist.name.take(2).uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Column {
            Text(
                text = artist.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (artist.lifeDates.isNotBlank()) {
                Text(
                    text = artist.lifeDates,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
