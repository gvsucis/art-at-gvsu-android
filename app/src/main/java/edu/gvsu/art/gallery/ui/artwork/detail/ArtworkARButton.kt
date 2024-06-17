package edu.gvsu.art.gallery.ui.artwork.detail

import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.gvsu.art.gallery.R
import edu.gvsu.art.gallery.lib.Async
import edu.gvsu.art.gallery.ui.theme.ArtAtGVSUTheme

@Composable
fun ArtworkARButton(
    arAsset: Async<ArtworkARAssets>,
    progress: Float,
    onRequestARAsset: () -> Unit = {},
) {
    IconButton(onClick = { onRequestARAsset() }) {
        if (arAsset.isLoading && progress > 0) {
            CircularProgressIndicator(
                progress = progress,
                strokeWidth = 3.dp,
                modifier = Modifier.size(20.dp),
                color = LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
            )
        } else {
            Icon(
                imageVector = Icons.Default.ViewInAr,
                contentDescription = stringResource(R.string.artwork_detail_view_in_ar)
            )
        }
    }
}

@Preview
@Composable
private fun ArtworkARButtonWithProgress() {

    ArtAtGVSUTheme {
        ArtworkARButton(
            arAsset = Async.Loading,
            progress = 0.67f
        )
    }
}
