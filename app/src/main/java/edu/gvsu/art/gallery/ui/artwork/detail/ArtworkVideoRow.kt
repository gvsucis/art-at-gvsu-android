package edu.gvsu.art.gallery.ui.artwork.detail

import android.widget.Space
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import edu.gvsu.art.client.SecondaryMedia
import edu.gvsu.art.gallery.R
import edu.gvsu.art.gallery.ui.theme.ArtGalleryTheme
import edu.gvsu.art.gallery.ui.theme.tertiaryLight
import java.net.URL

@Composable
fun ArtworkMultimediaRow(mediaItems: List<SecondaryMedia>, onClick: (link: URL) -> Unit) {
    Column(
        Modifier.padding(top = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.artwork_detail_multimedia),
            style = MaterialTheme.typography.headlineMedium,
            color = colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(Modifier.width(8.dp))
            mediaItems.forEach { media ->
                Box(
                    Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .clickable {
                            onClick(media.url)
                        }
                ) {
                    ArtworkVideoPlaceholder(url = media.thumbnailURL)
                }
            }
            Spacer(Modifier.height(8.dp).width(8.dp))
        }
    }
}

@Composable
fun ArtworkVideoPlaceholder(url: URL) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .aspectRatio(4 / 3f)
            .heightIn(max = 120.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(url.toString())
                .scale(Scale.FILL)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4 / 3f)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4 / 3f)
                .background(Color.Black.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                tint = Color.White,
                modifier = Modifier
                    .size(32.dp)
                    .background(tertiaryLight, CircleShape)
                    .padding(8.dp),
                contentDescription = null
            )
        }
    }
}

@Composable
@Preview
fun PreviewArtworkVideoRow() {
    val media = listOf(
        SecondaryMedia(
            URL("https://example.com/test"),
            URL("https://example.com/thumbnail")
        )
    )

    ArtGalleryTheme {
        ArtworkMultimediaRow(media) {}
    }
}