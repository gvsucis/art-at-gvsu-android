package edu.gvsu.art.gallery.ui

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import moe.tlaster.swiper.rememberSwiperState
import java.net.URL

@Composable
fun MediaScreen(
    urls: List<URL>,
    pagerState: PagerState,
    onDismiss: () -> Unit = {},
) {
    val view = LocalView.current

    var controlVisibility by remember { mutableStateOf(true) }
    val swiperState = rememberSwiperState(
        onDismiss = {
            onDismiss()
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 1f - swiperState.progress))
    ) {
        Box {
            MediaView(
                urls = urls,
                swiperState = swiperState,
                pagerState = pagerState,
                videoControlVisibility = controlVisibility && swiperState.progress == 0f,
                onClick = { controlVisibility = !controlVisibility }
            )
        }
        Box(
            Modifier.statusBarsPadding()
        ) {
            CloseButton(
                onClick = { onDismiss() },
                visible = controlVisibility && swiperState.progress == 0f
            )
        }
    }

    SideEffect {
        val window = (view.context as Activity).window

        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
    }

    DisposableEffect(Unit) {
        val window = (view.context as Activity).window

        val previousAppearanceLightStatusBars =
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars

        onDispose {
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                previousAppearanceLightStatusBars
        }
    }
}

@Composable
private fun CloseButton(
    onClick: () -> Unit,
    visible: Boolean,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = shrinkVertically() + fadeOut()
    ) {
        CloseIconButton(onClick = { onClick() })
    }
}
