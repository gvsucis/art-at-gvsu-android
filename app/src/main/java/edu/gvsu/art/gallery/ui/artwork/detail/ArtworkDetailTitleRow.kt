package edu.gvsu.art.gallery.ui.artwork.detail

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.gvsu.art.client.Artwork
import edu.gvsu.art.gallery.R
import edu.gvsu.art.gallery.extensions.shareArtwork
import edu.gvsu.art.gallery.ui.TitleText
import edu.gvsu.art.gallery.ui.theme.ArtAtGVSUTheme
import edu.gvsu.art.gallery.ui.theme.Red
import java.net.URL

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ArtworkDetailTitleRow(
    artwork: Artwork,
    isFavorite: Boolean = false,
    toggleFavorite: () -> Unit = {},
) {
    val context = LocalContext.current

    val (arAsset, requestARAsset) = rememberARAsset(artwork) { uri ->
        Log.d("ArtworkDetailTitleRow", "URI: $uri")

        Intent(context, ArtworkARActivity::class.java).apply {
            putExtra("EXTRA_AR_ASSET_PATH", uri.toString())

            context.startActivity(this)
        }
    }

    FlowRow(
        verticalArrangement = Arrangement.Center,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.heightIn(min = 48.dp)
        ) {
            TitleText(text = artwork.name)
        }
        Row {
            if (artwork.hasAR) {
                ArtworkARButton(
                    arAsset = arAsset,
                    onRequestARAsset = requestARAsset,
                )
            }
            IconButton(
                onClick = { toggleFavorite() }
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    tint = Red,
                    contentDescription = null
                )
            }
            IconButton(onClick = { context.shareArtwork(artwork) }) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = stringResource(R.string.artwork_detail_share)
                )
            }
        }
    }
}

@Composable
@Preview
fun PreviewArtworkDetailTitleRow() {
    val artwork = Artwork(name = "My Artwork")
    ArtAtGVSUTheme {
        ArtworkDetailTitleRow(artwork = artwork)
    }
}


@Composable
@Preview("Long title that could wrap")
fun PreviewArtworkDetailTitleRowLongText() {
    val artwork = Artwork(name = "My Artwork Continuous Stream of Words")
    ArtAtGVSUTheme {
        ArtworkDetailTitleRow(artwork = artwork)
    }
}

@Composable
@Preview("AR Available")
fun PreviewArtworkDetailTitleWithAR() {
    val artwork = Artwork(
        name = "My Life's Work",
        arDigitalAssetURL = URL("https://example.com")
    )
    ArtAtGVSUTheme {
        ArtworkDetailTitleRow(artwork = artwork)
    }
}
