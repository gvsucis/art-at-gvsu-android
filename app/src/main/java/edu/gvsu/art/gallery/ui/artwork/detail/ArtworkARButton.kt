package edu.gvsu.art.gallery.ui.artwork.detail

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import edu.gvsu.art.gallery.R
import edu.gvsu.art.gallery.lib.Async
import edu.gvsu.art.gallery.ui.theme.ArtGalleryTheme

@Composable
fun ArtworkARButton(
    onRequestARAsset: () -> Unit = {},
) {
    IconButton(onClick = { onRequestARAsset() }) {
        Icon(
            imageVector = Icons.Default.ViewInAr,
            contentDescription = stringResource(R.string.artwork_detail_view_in_ar)
        )
    }
}

@Preview
@Composable
private fun ArtworkARButtonWithProgress() {
    ArtGalleryTheme {
        ArtworkARButton {}
    }
}
