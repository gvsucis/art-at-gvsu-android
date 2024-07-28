package edu.gvsu.art.gallery.ui.foundation

import android.content.Context
import android.view.SurfaceView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL
import androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
import androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
import androidx.media3.ui.PlayerView
import edu.gvsu.art.gallery.lib.CacheDataSourceFactory
import edu.gvsu.art.gallery.lib.VideoPool
import edu.gvsu.art.gallery.ui.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@UnstableApi
class MediaPlayerView(
    url: String,
    zOrderMediaOverlay: Boolean,
    keepScreenOn: Boolean,
    videoPool: VideoPool,
) {
    private var job: Job? = null

    private val scope = CoroutineScope(Dispatchers.Main)

    private val context = get<Context>()

    private var playerProgressCallBack: PlayerProgressCallBack? = null
    private var playerCallBack: PlayerCallBack? = null

    private var androidPlayer = PlayerView(context).also { playerView ->
        (playerView.videoSurfaceView as? SurfaceView)?.setZOrderMediaOverlay(zOrderMediaOverlay)
        playerView.resizeMode = RESIZE_MODE_FILL
        playerView.useController = false
        playerView.keepScreenOn = keepScreenOn
    }.apply {
        player = ExoPlayer.Builder(context).build()
            .apply {
                repeatMode = Player.REPEAT_MODE_ALL
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        when (state) {
                            Player.STATE_BUFFERING -> {
                                playerCallBack?.onBuffering()
                            }

                            Player.STATE_READY -> {
                                playerCallBack?.onReady()
                            }

                            else -> {}
                        }
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        playerCallBack?.onIsPlayingChanged(isPlaying)
                        job?.cancel()
                        if (isPlaying) {
                            job = scope.launch {
                                while (true) {
                                    delay(1000)
                                    playerProgressCallBack?.onTimeChanged(contentPosition())
                                    videoPool.set(url, contentPosition())
                                }
                            }
                        }
                    }
                })

                ProgressiveMediaSource.Factory(
                    CacheDataSourceFactory(
                        context,
                        5L * 1024L * 1024L,
                    )
                ).createMediaSource(MediaItem.fromUri(url)).also {
                    setMediaSource(it)
                }
                playerCallBack?.onPrepareStart()
                prepare()
                seekTo(videoPool.get(url))
            }
    }

    fun play() {
        androidPlayer.player?.playWhenReady = true
        androidPlayer.onResume()
    }

    fun pause() {
        androidPlayer.player?.playWhenReady = false
        androidPlayer.onPause()
    }

    fun contentPosition(): Long = 0L.coerceAtLeast((androidPlayer.player?.currentPosition) ?: 0)

    fun setVolume(volume: Float) {
        androidPlayer.player?.volume = volume
    }

    fun release() {
        job?.cancel()
        playerCallBack = null
        playerProgressCallBack = null
        androidPlayer.player?.release()
    }

    fun duration(): Long = androidPlayer.player?.duration ?: 0
    fun seekTo(time: Long) {
        androidPlayer.player?.seekTo(time)
    }

    fun setMute(mute: Boolean) {
        androidPlayer.player?.volume = if (mute) 0f else 1f
    }

    @Composable
    fun Content(update: () -> Unit = {}, modifier: Modifier) {
        AndroidView(
            factory = {
                androidPlayer
            },
            modifier = modifier,
            update = {
                update.invoke()
            }
        )
    }

    fun registerPlayerCallback(callBack: PlayerCallBack) {
        playerCallBack = callBack
    }

    fun registerProgressCallback(callBack: PlayerProgressCallBack) {
        playerProgressCallBack = callBack
    }

    fun removePlayerCallback(callback: PlayerCallBack) {
        playerCallBack = null
    }

    fun removeProgressCallback(callback: PlayerProgressCallBack) {
        playerProgressCallBack = null
    }
}
