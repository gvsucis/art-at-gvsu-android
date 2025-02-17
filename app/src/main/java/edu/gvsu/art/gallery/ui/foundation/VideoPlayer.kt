package edu.gvsu.art.gallery.ui.foundation


import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.util.UnstableApi

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    videoState: VideoPlayerState,
    playEnable: Boolean = false,
    zOrderMediaOverlay: Boolean = false,
    keepScreenOn: Boolean = false,
    thumb: @Composable (() -> Unit)? = null,
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val videoPool = LocalVideoPool.current

    Box {
        val mediaPlayerView = remember(videoState.url) {
            MediaPlayerView(
                url = videoState.url,
                zOrderMediaOverlay = zOrderMediaOverlay,
                keepScreenOn = keepScreenOn,
                videoPool = videoPool,
            ).apply {
                videoState.bind(this)
            }
        }

        DisposableEffect(Unit) {
            val observer = object : DefaultLifecycleObserver {
                override fun onResume(owner: LifecycleOwner) {
                    super.onResume(owner)
                    videoState.onResume()
                }

                override fun onPause(owner: LifecycleOwner) {
                    super.onPause(owner)
                    videoState.onPause()
                }
            }
            lifecycle.addObserver(observer)
            onDispose {
                videoState.onPause()
                mediaPlayerView.release()
                lifecycle.removeObserver(observer)
            }
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            mediaPlayerView.Content(
                modifier = Modifier
                    .aspectRatio(16 / 9f)
                    .fillMaxSize()
            )
        }
    }
    if ((videoState.showThumbnail || !playEnable) && thumb != null) {
        thumb()
    }
    if (videoState.showLoading && playEnable) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }

    if (!playEnable) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                tint = Color.White.copy(alpha = LocalContentColor.current.alpha),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentDescription = null
            )
        }
    }
}

interface PlayerCallBack {
    fun onPrepareStart() // start to prepare video
    fun onReady() // ready to play video
    fun onIsPlayingChanged(isPlaying: Boolean) // video play/pause
    fun onBuffering() // video buffering
}

interface PlayerProgressCallBack {
    fun onTimeChanged(time: Long)
}
