package edu.gvsu.art.gallery.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import edu.gvsu.art.client.Artist
import edu.gvsu.art.gallery.lib.Async
import edu.gvsu.art.gallery.navigateToArtworkDetail
import edu.gvsu.art.gallery.ui.foundation.LocalTabScreen
import edu.gvsu.art.gallery.ui.theme.ArtAtGVSUTheme

@Composable
fun ArtistDetailScreen(navController: NavController, artistID: String?) {
    artistID ?: return

    val currentTab = LocalTabScreen.current
    fun navigateToArtwork(artworkID: String) {
        navController.navigateToArtworkDetail(currentTab, artworkID)
    }

    val artist = when (val data = useArtist(artistID)) {
        is Async.Success -> data()
        else -> Artist()
    }
    val artistWorks = artist.relatedWorks
    val scrollState = rememberLazyListState()
    Column {
        GalleryTopAppBar(
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                }
            }
        )
        LazyColumn(state = scrollState) {
            item(key = "artist_name") {
                TitleText(
                    text = artist.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                )
            }
            item(key = "artist_biography") {
                ArtistDetailBiography(artist = artist)
            }
            items(artistWorks, key = { "artwork:${it.id}" }) { artwork ->
                Column {
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
}

@Composable
private fun ArtistDetailBiography(artist: Artist) {
    Column(modifier = Modifier
        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        if (artist.lifeDates.isNotBlank()) {
            Text(artist.lifeDates)
        }
        if (artist.nationality.isNotBlank()) {
            Text(artist.nationality)
        }
        if (artist.biography.isNotBlank()) {
            Text(artist.biography)
        }
        Spacer(Modifier.height(8.dp))
        Divider()
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

    ArtAtGVSUTheme(darkTheme = false) {
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

    ArtAtGVSUTheme(darkTheme = true) {
        Surface {
            ArtistDetailBiography(artist = artist)
        }
    }
}
