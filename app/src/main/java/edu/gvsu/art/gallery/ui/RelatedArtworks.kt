package edu.gvsu.art.gallery.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.gvsu.art.client.Artwork

@Composable
fun RelatedArtworks(
    artworks: List<Artwork>,
    onArtworkSelect: (artworkID: String) -> Unit = {},
) {
    Column {
        artworks.forEach { artwork ->
            ArtworkRow(
                artwork = artwork,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onArtworkSelect(artwork.id) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}
