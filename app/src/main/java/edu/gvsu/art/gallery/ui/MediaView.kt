package edu.gvsu.art.gallery.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.mxalbert.zoomable.Zoomable
import edu.gvsu.art.gallery.lib.MediaTypes
import edu.gvsu.art.gallery.ui.foundation.VideoPlayer
import edu.gvsu.art.gallery.ui.foundation.rememberVideoPlayerState
import moe.tlaster.swiper.Swiper
import moe.tlaster.swiper.SwiperState
import java.net.URL

@Composable
fun MediaView(
    urls: List<URL>,
    swiperState: SwiperState,
    pagerState: PagerState,
    videoControlVisibility: Boolean,
    onClick: () -> Unit,
) {
    val (videoUrl, setVideoUrl) = remember { mutableStateOf<URL?>(null) }
    val videoState = rememberVideoPlayerState(
        url = videoUrl?.toString()
    )

    LaunchedEffect(pagerState.currentPage) {
        val url = urls.getOrNull(pagerState.currentPage) ?: return@LaunchedEffect

        if (MediaTypes.isVideo(url)) {
            setVideoUrl(url)
        } else {
            setVideoUrl(null)
        }
    }

    Swiper(state = swiperState) {
        HorizontalPager(
            pageSpacing = 8.dp,
            state = pagerState,
            key = { urls[it] },
        ) { page ->
            val url = urls[page]
            if (MediaTypes.isVideo(url)) {
                Box(modifier = Modifier.pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onClick() }
                    )
                }) {
                    videoState?.let {
                        VideoPlayer(
                            videoState = it,
                            playEnable = true,
                            zOrderMediaOverlay = true,
                            keepScreenOn = true,
                        )
                        AnimatedVisibility(
                            visible = videoControlVisibility,
                            enter = fadeIn() + expandVertically(),
                            exit = shrinkVertically() + fadeOut(),
                            modifier = Modifier.align(Alignment.BottomStart)
                        ) {
                            VideoControl(state = it)
                        }
                    }
                }
            } else {
                Zoomable(onTap = onClick) {
                    RemoteImage(url = url)
                }
            }
        }
    }
}

@Composable
private fun RemoteImage(url: URL) {
    val modifier = Modifier.fillMaxSize()
    val painter = rememberAsyncImagePainter(model = url.toString())
    Box {
        val size = painter.intrinsicSize
        Image(
            painter = painter,
            modifier = if (size != Size.Unspecified) Modifier
                .aspectRatio(size.width / size.height)
                .then(modifier) else modifier,
            contentScale = ContentScale.Fit,
            contentDescription = null
        )
    }
}
