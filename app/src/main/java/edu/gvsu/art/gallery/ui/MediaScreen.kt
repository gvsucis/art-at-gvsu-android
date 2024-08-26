package edu.gvsu.art.gallery.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import moe.tlaster.swiper.rememberSwiperState
import java.net.URL

@Composable
fun MediaScreen(
    urls: List<URL>,
    pagerState: PagerState,
    onDismiss: () -> Unit = {},
) {
    var controlVisibility by remember { mutableStateOf(true) }
    val swiperState = rememberSwiperState(
        onDismiss = {
            onDismiss()
        }
    )

    Box(modifier = Modifier
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
        CloseButton(
            onClick = { onDismiss() },
            visible = controlVisibility && swiperState.progress == 0f
        )
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
