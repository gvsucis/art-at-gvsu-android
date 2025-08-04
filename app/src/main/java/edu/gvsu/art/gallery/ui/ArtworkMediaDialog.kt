package edu.gvsu.art.gallery.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import edu.gvsu.art.gallery.lib.VideoPool
import edu.gvsu.art.gallery.ui.foundation.LocalVideoPool
import edu.gvsu.art.gallery.ui.mediaviewer.LocalMediaViewerState

@ExperimentalComposeUiApi
@Composable
fun ArtworkMediaDialog() {
    val mediaViewer = LocalMediaViewerState.current
    val media = mediaViewer.links.orEmpty()

    AnimatedVisibility(
        enter = fadeIn(),
        exit = fadeOut(),
        visible = media.isNotEmpty()
    ) {
        val pagerState = rememberPagerState(initialPage = mediaViewer.currentIndex) {
            media.size
        }

        val videoPool = rememberSaveable { VideoPool() }

        CompositionLocalProvider(LocalVideoPool provides videoPool) {
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
