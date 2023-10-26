package edu.gvsu.art.gallery.ui.foundation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun rememberVideoPlayerState(
    url: String?,
    isPlaying: Boolean = false,
    volume: Float = 1f,
    isReady: Boolean = false,
    currentPosition: Long = 0L,
    isMute: Boolean = false,
): VideoPlayerState? {
    if (url.isNullOrEmpty()) {
        return null
    }

    return rememberSaveable(
        saver = VideoPlayerState.Saver(url),
        key = url
    ) {
        VideoPlayerState(
            url = url,
            isPlaying = isPlaying,
            volume = volume,
            isReady = isReady,
            currentPosition = currentPosition,
            isMute = isMute
        )
    }
}

@Stable
class VideoPlayerState(
    val url: String,
    isReady: Boolean,
    isPlaying: Boolean,
    currentPosition: Long,
    volume: Float,
    isMute: Boolean,
) {
    private lateinit var player: PlayerView

    private var _isReady = mutableStateOf(isReady)
    var isReady
        get() = _isReady.value
        set(value) {
            _isReady.value = value
        }
    private var _isBuffering = mutableStateOf(false)
    var isBuffering
        get() = _isBuffering.value
        set(value) {
            _isBuffering.value = value
        }
    private var _isPlaying = mutableStateOf(isPlaying)
    var isPlaying
        get() = _isPlaying.value
        set(value) {
            _isPlaying.value = value
        }

    private var seeking = false

    private var _currentPosition = mutableStateOf(currentPosition)
    var currentPosition
        get() = _currentPosition.value
        set(value) {
            _currentPosition.value = value
        }

    private val progressCallBack = object : PlayerProgressCallBack {
        override fun onTimeChanged(time: Long) {
            if (!seeking) _currentPosition.value = time
        }
    }

    private val playerCallBack = object : PlayerCallBack {
        override fun onPrepareStart() {
            _isReady.value = false
        }

        override fun onReady() {
            _isReady.value = true
            _isBuffering.value = false
            initPlay()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
            _isBuffering.value = false
        }

        override fun onBuffering() {
            _isBuffering.value = true
        }
    }

    private var _volume = mutableStateOf(volume)
    var volume
        get() = _volume.value
        set(value) {
            _volume.value = value
            player.setVolume(value)
        }

    private var _isMute = mutableStateOf(isMute)
    var isMute
        get() = _isMute.value
        set(value) {
            _isMute.value = value
            player.setMute(value)
        }

    val duration get() = player.duration().coerceAtLeast(0)
    val showThumbnail get() = !isReady
    val showLoading get() = !isReady || isBuffering

    companion object {
        fun Saver(url: String): Saver<VideoPlayerState, *> = listSaver(
            save = {
                listOf<Any>(
                    it.isReady,
                    it.isPlaying,
                    it.currentPosition,
                    it.volume,
                    it.isMute
                )
            },
            restore = {
                VideoPlayerState(
                    url = url,
                    isReady = it[0] as Boolean,
                    isPlaying = it[1] as Boolean,
                    currentPosition = it[2] as Long,
                    volume = it[3] as Float,
                    isMute = it[4] as Boolean
                )
            }
        )
    }

    private fun initPlay() {
        player.setVolume(volume)
        player.setMute(isMute)
        if (isReady) player.play()
    }

    fun bind(player: PlayerView) {
        this.player = player
        initPlay()
    }

    internal fun onResume() {
        player.registerProgressCallback(progressCallBack)
        player.registerPlayerCallback(playerCallBack)
        if (isPlaying) player.play()
    }

    internal fun onPause() {
        player.removeProgressCallback(progressCallBack)
        player.removePlayerCallback(playerCallBack)
        // remove callback first then pause, so state can store the playing state before pause
        player.pause()
    }

    fun playSwitch() {
        if (isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    fun seeking() {
        seeking = true
    }

    fun seekTo(time: Long) {
        player.seekTo(time)
        seeking = false
    }

    fun mute() {
        isMute = !isMute
    }
}
