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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import edu.gvsu.art.gallery.lib.VideoPool
import edu.gvsu.art.gallery.ui.foundation.LocalVideoPool
import edu.gvsu.art.gallery.ui.mediaviewer.LocalMediaViewerState

@ExperimentalComposeUiApi
@Composable
fun ArtworkMediaDialog() {
    val mediaViewer = LocalMediaViewerState.current
    val artwork = mediaViewer.artwork ?: return

    val view = LocalView.current

    AnimatedVisibility(
        enter = fadeIn(),
        exit = fadeOut(),
        visible = mediaViewer.artwork != null
    ) {

        val videoPool = rememberSaveable() { VideoPool() }

        CompositionLocalProvider(
            LocalVideoPool provides videoPool
        ) {
            val pagerState = rememberPagerState(initialPage = mediaViewer.currentIndex) {
                artwork.mediaRepresentations.size
            }

            MediaScreen(
                urls = artwork.mediaRepresentations,
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

    DisposableEffect(artwork.id) {
        val window = (view.context as Activity).window

        val previousAppearanceLightStatusBars =
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars

        onDispose {
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                previousAppearanceLightStatusBars
        }
    }

    SideEffect {
        val window = (view.context as Activity).window

        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
    }
}
