package edu.gvsu.art.gallery.ui.mediaviewer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import edu.gvsu.art.client.Artwork

val LocalMediaViewerState = compositionLocalOf { MediaViewerState() }

@Composable
fun rememberMediaViewerState(): MediaViewerState {
    return rememberSaveable(
        saver = MediaViewerState.Saver()
    ) {
        MediaViewerState()
    }
}

@Stable
class MediaViewerState(
    private val initialArtwork: Artwork? = null,
    private var initialIndex: Int = 0,
) {
    var currentIndex by mutableIntStateOf(initialIndex)
        private set
    var artwork by mutableStateOf(initialArtwork)
        private set

    fun present(artwork: Artwork, currentIndex: Int) {
        this.artwork = artwork
        this.currentIndex = currentIndex
    }

    fun updateIndex(index: Int) {
        currentIndex = index
    }

    fun close() {
        this.artwork = null
        this.currentIndex = 0
    }

    companion object {
        fun Saver(): Saver<MediaViewerState, *> = listSaver(
            save = {
                listOf(
                    it.artwork,
                    it.currentIndex
                )
            },
            restore = {
                MediaViewerState(
                    initialArtwork = it[0] as? Artwork?,
                    initialIndex = it[1] as? Int ?: 0,
                )
            }
        )
    }
}
