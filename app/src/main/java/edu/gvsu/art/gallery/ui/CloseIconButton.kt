package edu.gvsu.art.gallery.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.gvsu.art.gallery.ui.theme.ArtGalleryTheme

@Composable
fun CloseIconButton(
    style: CloseIconStyle = CloseIconStyle.Close,
    onClick: () -> Unit = {}
) {
    val icon = when (style) {
        CloseIconStyle.Close -> Icons.Default.Close
        CloseIconStyle.Back -> Icons.Default.ArrowBack
    }

    Box(
        modifier = Modifier.padding(4.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.TopStart)
                .clip(CircleShape)
                .background(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = CircleShape
                )
                .clickable { onClick() }
                .clipToBounds()
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}

enum class CloseIconStyle {
    Close,
    Back,
}

@Preview
@Composable
private fun CloseIconButtonPreview() {
    ArtGalleryTheme {
        CloseIconButton()
    }
}
