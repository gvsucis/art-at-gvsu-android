package edu.gvsu.art.gallery.ui

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import edu.gvsu.art.gallery.lib.VideoPool
import edu.gvsu.art.gallery.ui.foundation.LocalVideoPool
import edu.gvsu.art.gallery.ui.mediaviewer.LocalMediaViewerState

@ExperimentalComposeUiApi
@Composable
fun ArtworkMediaDialog() {
    val videoPool = rememberSaveable { VideoPool() }
    val mediaViewer = LocalMediaViewerState.current
    val media = mediaViewer.artwork?.mediaRepresentations.orEmpty()

    CompositionLocalProvider(LocalVideoPool provides videoPool) {
        AnimatedVisibility(
            enter = fadeIn(),
            exit = fadeOut(),
            visible = mediaViewer.artwork != null
        ) {
            val pagerState = rememberPagerState(initialPage = mediaViewer.currentIndex) {
                media.size
            }

            MediaScreen(
                urls = media,
                pagerState = pagerState,
                onDismiss = {
                    mediaViewer.close()
                }
            )

            LaunchedEffect(pagerState.currentPage) {
                mediaViewer.updateIndex(pagerState.currentPage)
            }
        }
    }
}
