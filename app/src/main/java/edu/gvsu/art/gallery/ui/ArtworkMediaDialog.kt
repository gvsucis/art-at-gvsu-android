package edu.gvsu.art.gallery.ui

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import edu.gvsu.art.client.Artwork

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@ExperimentalComposeUiApi
@ExperimentalPagerApi
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
        Scaffold(backgroundColor = Color.Transparent) {
            MediaScreen(
                urls = artwork.mediaRepresentations,
                pagerState = pagerState,
                onDismiss = { onDismiss() }
            )
        }
    }

    LaunchedEffect(selectedPage) {
        pagerState.scrollToPage(selectedPage)
    }
}
