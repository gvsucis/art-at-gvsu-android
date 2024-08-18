package edu.gvsu.art.gallery.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import edu.gvsu.art.gallery.ui.theme.ArtGalleryTheme
import java.net.URL

@Composable
fun ArtworkVideoPlaceholder(url: URL?) {
    Box(contentAlignment = Alignment.Center) {
        AsyncImage(
            model = url.toString(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(radius = 10.dp)
        )
        Box(Modifier.background(Color.Black.copy(alpha = 0.2f)))
        Icon(
            imageVector = Icons.Default.PlayArrow,
            tint = Color.White,
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colors.primary, CircleShape)
                .padding(8.dp),
            contentDescription = null
        )
    }
}

@Preview
@Composable
private fun ArtworkVideoPlaceholderPreview() {
    val url =
        URL("https:/artgallery.gvsu.edu/admin/media/collectiveaccess/images/1/4/4/5337_ca_object_representations_media_14448_large.jpg")
    ArtGalleryTheme {
        ArtworkVideoPlaceholder(
            url = url
        )
    }
}
