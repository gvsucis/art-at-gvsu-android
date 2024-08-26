package edu.gvsu.art.gallery.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import edu.gvsu.art.client.Artwork

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@ExperimentalComposeUiApi
@Composable
fun ArtworkMediaDialog(
    artwork: Artwork,
    pagerState: PagerState,
    selectedPage: Int,
    onDismiss: () -> Unit = {},
) {
    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        MediaScreen(
            urls = artwork.mediaRepresentations,
            pagerState = pagerState,
            onDismiss = { onDismiss() }
        )
    }

    LaunchedEffect(selectedPage) {
        pagerState.scrollToPage(selectedPage)
    }
}
