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
import java.net.URL

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
    private val initialLinks: List<URL>? = null,
    private var initialIndex: Int = 0,
) {
    var currentIndex by mutableIntStateOf(initialIndex)
        private set
    var links by mutableStateOf(initialLinks)
        private set

    fun present(links: List<URL>, currentIndex: Int = 0) {
        this.links = links
        this.currentIndex = currentIndex
    }

    fun updateIndex(index: Int) {
        currentIndex = index
    }

    fun close() {
        this.links = null
        this.currentIndex = 0
    }

    companion object {
        fun Saver(): Saver<MediaViewerState, *> = listSaver(
            save = {
                listOf(
                    it.links,
                    it.currentIndex
                )
            },
            restore = {
                MediaViewerState(
                    initialLinks = it[0] as? List<URL>? ?: emptyList(),
                    initialIndex = it[1] as? Int ?: 0,
                )
            }
        )
    }
}
