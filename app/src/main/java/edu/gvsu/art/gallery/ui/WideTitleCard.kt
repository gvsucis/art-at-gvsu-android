package edu.gvsu.art.gallery.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import edu.gvsu.art.gallery.ui.theme.ArtGalleryTheme
import edu.gvsu.art.gallery.ui.theme.OffWhite
import java.net.URL

@Composable
fun WideTitleCard(
    title: String,
    subtitle: String = "",
    imageURL: URL?,
    onClick: () -> Unit = {},
) {
    val cornerShape = RoundedCornerShape(10.dp)

    Surface(
        shape = cornerShape,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3 / 2f)
                .clickable { onClick() }
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageURL.toString())
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3 / 2f)
                    .clip(cornerShape)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .fillMaxHeight(fraction = 0.5f)
                    .clip(cornerShape)
                    .background(Brush.verticalGradient(
                        0f to Color.Transparent,
                        1.0f to Color.Black.copy(alpha = 0.6f)
                    ))
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    title,
                    color = OffWhite,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                if (subtitle.isNotBlank()) {
                    Text(subtitle,
                        color = OffWhite.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}


@Composable
@Preview
fun PreviewWideTitleRow() {
    ArtGalleryTheme {
        WideTitleCard(
            imageURL = null,
            title = "My Card Title",
            subtitle = "My card subtitle"
        )
    }
}
