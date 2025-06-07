package edu.gvsu.art.gallery.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.exitUntilCollapsedScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import edu.gvsu.art.client.Artist
import edu.gvsu.art.gallery.extensions.nestedScaffoldPadding
import edu.gvsu.art.gallery.lib.Async
import edu.gvsu.art.gallery.navigateToArtworkDetail
import edu.gvsu.art.gallery.ui.foundation.LocalTopLevelRoute
import edu.gvsu.art.gallery.ui.theme.ArtGalleryTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(navController: NavController, artistID: String?) {
    artistID ?: return

    val currentTab = LocalTopLevelRoute.current
    fun navigateToArtwork(artworkID: String) {
        navController.navigateToArtworkDetail(currentTab, artworkID)
    }

    val artist = when (val data = useArtist(artistID)) {
        is Async.Success -> data()
        else -> Artist()
    }
    val artistWorks = artist.relatedWorks
    val scrollState = rememberLazyListState()
    val scrollBehavior = exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = { Text(artist.name) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.background),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        }
    ) { padding ->
        LazyColumn(
            state = scrollState,
            modifier = Modifier.nestedScaffoldPadding(padding)
        ) {
            item(key = "artist_biography") {
                ArtistDetailBiography(artist = artist)
                HorizontalDivider()
            }

            itemsIndexed(
                artistWorks,
                key = { _, artwork -> "artwork:${artwork.id}" }
            ) { index, artwork ->
                if (index == 0) {
                    Spacer(Modifier.height(8.dp))
                }

                ArtworkRow(
                    artwork = artwork,
                    modifier = Modifier
                        .clickable { navigateToArtwork(artwork.id) }
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun ArtistDetailBiography(artist: Artist) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (artist.lifeDates.isNotBlank()) {
                Text(
                    artist.lifeDates,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (artist.nationality.isNotBlank()) {
                Text(artist.nationality)
            }
        }

        if (artist.biography.isNotBlank()) {
            Text(artist.biography)
        }
    }
}

@Composable
@Preview("ArtistDetailBiography light")
fun PreviewArtistDetailBiography() {
    val artist = Artist(
        id = "637",
        name = "Alten, Mathias Joseph",
        nationality = "German American",
        lifeDates = "1871-1938",
        biography = "Alten's view of Grand Rapids captured the expansion and development" +
                " of a city through images including its downtown streets," +
                " Reeds Lake and Alten's own backyard..."
    )

    ArtGalleryTheme(darkTheme = false) {
        Surface {
            ArtistDetailBiography(artist = artist)
        }
    }
}


@Composable
@Preview("ArtistDetailBiography dark")
fun PreviewArtistDetailBiographyDark() {
    val artist = Artist(
        id = "637",
        name = "Alten, Mathias Joseph",
        nationality = "German American",
        lifeDates = "1871-1938",
        biography = "Alten's view of Grand Rapids captured the expansion and development" +
                " of a city through images including its downtown streets," +
                " Reeds Lake and Alten's own backyard..."
    )

    ArtGalleryTheme(darkTheme = true) {
        Surface {
            ArtistDetailBiography(artist = artist)
        }
    }
}
