package edu.gvsu.art.gallery.ui.foundation

import android.content.Context
import android.view.SurfaceView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.StyledPlayerView
import edu.gvsu.art.gallery.lib.CacheDataSourceFactory
import edu.gvsu.art.gallery.lib.VideoPool
import edu.gvsu.art.gallery.ui.get
import kotlinx.coroutines.*

class PlayerView constructor(
    url: String,
    zOrderMediaOverlay: Boolean,
    keepScreenOn: Boolean,
    backgroundColor: Color?,
    onClick: (() -> Unit)?
) {
    private var job: Job? = null

    private val scope = CoroutineScope(Dispatchers.Main)

    private val context = get<Context>()

    private var playerProgressCallBack: PlayerProgressCallBack? = null
    private var playerCallBack: PlayerCallBack? = null

    private var androidPlayer = StyledPlayerView(context).also { playerView ->
        (playerView.videoSurfaceView as? SurfaceView)?.setZOrderMediaOverlay(zOrderMediaOverlay)
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
                seekTo(VideoPool.get(url))
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
    fun Content(modifier: Modifier, update: () -> Unit) {
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
