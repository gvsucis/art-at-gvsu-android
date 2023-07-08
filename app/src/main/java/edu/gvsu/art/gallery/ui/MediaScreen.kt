package edu.gvsu.art.gallery.ui

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import moe.tlaster.swiper.rememberSwiperState
import java.net.URL


@ExperimentalAnimationApi
@SuppressLint("UnrememberedMutableState")
@ExperimentalPagerApi
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
