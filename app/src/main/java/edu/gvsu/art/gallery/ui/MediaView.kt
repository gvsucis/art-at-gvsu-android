package edu.gvsu.art.gallery.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.mxalbert.zoomable.Zoomable
import edu.gvsu.art.gallery.lib.MediaTypes
import edu.gvsu.art.gallery.ui.foundation.VideoPlayer
import edu.gvsu.art.gallery.ui.foundation.rememberRemoteImage
import edu.gvsu.art.gallery.ui.foundation.rememberVideoPlayerState
import moe.tlaster.swiper.Swiper
import moe.tlaster.swiper.SwiperState
import java.net.URL

@ExperimentalAnimationApi
@ExperimentalPagerApi
@Composable
fun MediaView(
    urls: List<URL>,
    volume: Float = 1f,
    swiperState: SwiperState,
    pagerState: PagerState,
    videoControlVisibility: Boolean,
    onClick: () -> Unit,
) {
    Swiper(state = swiperState) {
        HorizontalPager(
            itemSpacing = 8.dp,
            count = urls.size,
            state = pagerState,
            key = { urls[it] }
        ) { page ->
            val url = urls[page]
            if (MediaTypes.isVideo(url)) {
                Box(modifier = Modifier.pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onClick() }
                    )
                }) {
                    val videoState = rememberVideoPlayerState(
                        url = url.toString(),
                        volume = volume,
                        isMute = false
                    )
                    VideoPlayer(
                        playEnable = currentPage == page,
                        videoState = videoState,
                        zOrderMediaOverlay = true,
                        keepScreenOn = true,
                        backgroundColor = Color.Blue,
                    )
                    AnimatedVisibility(
                        visible = videoControlVisibility,
                        enter = fadeIn() + expandVertically(),
                        exit = shrinkVertically() + fadeOut(),
                        modifier = Modifier.align(Alignment.BottomStart)
                    ) {
                        VideoControl(state = videoState)
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
    val painter = rememberRemoteImage(url = url)
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
