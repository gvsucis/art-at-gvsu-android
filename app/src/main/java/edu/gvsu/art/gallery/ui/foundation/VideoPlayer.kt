package edu.gvsu.art.gallery.ui.foundation


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

@Composable
fun VideoPlayer(
    modifier: Modifier = Modifier.fillMaxSize(), // must set video player size
    videoState: VideoPlayerState,
    playEnable: Boolean = true,
    zOrderMediaOverlay: Boolean = false,
    keepScreenOn: Boolean = false,
    thumb: @Composable() (() -> Unit)? = null,
    backgroundColor: Color? = null,
    onClick: (() -> Unit)? = null,
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    Box {
        if (playEnable) {
            val playerView = remember(videoState.url) {
                PlayerView(
                    url = videoState.url,
                    zOrderMediaOverlay = zOrderMediaOverlay,
                    keepScreenOn = keepScreenOn,
                    backgroundColor = backgroundColor,
                    onClick = onClick
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
                    playerView.release()
                    lifecycle.removeObserver(observer)
                }
            }

            Box {
                playerView.Content(modifier = modifier) {}
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
                    tint = Color.White.copy(alpha = LocalContentAlpha.current),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(44.dp)
                        .background(MaterialTheme.colors.primary, CircleShape),
                    contentDescription = null
                )
            }
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
