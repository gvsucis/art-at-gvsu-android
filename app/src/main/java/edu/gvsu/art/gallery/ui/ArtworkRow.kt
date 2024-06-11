package edu.gvsu.art.gallery.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import edu.gvsu.art.client.Artwork

@Composable
fun ArtworkRow(artwork: Artwork, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = artwork.thumbnail.toString(),
            contentScale = ContentScale.Crop,
            contentDescription = null,
            modifier = Modifier
                .width(48.dp)
                .aspectRatio(1f)
                .fillMaxWidth()
                .clip(CircleShape)
                .border(BorderStroke(2.dp, MaterialTheme.colors.onSurface), CircleShape)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            artwork.name,
            textAlign = TextAlign.Start
        )
    }
}
