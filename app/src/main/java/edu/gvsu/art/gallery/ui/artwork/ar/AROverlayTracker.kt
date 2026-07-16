package edu.gvsu.art.gallery.ui.artwork.ar

import androidx.compose.runtime.mutableStateMapOf

/**
 * Tracks which artwork overlays are live, bounded to [maxActive] by LRU eviction. [overlays]
 * is a snapshot-backed map, so reading it in a composable recomposes as overlays mount and
 * evict.
 */
class AROverlayTracker<T>(private val maxActive: Int) {
    private val active = mutableStateMapOf<String, T>()
    private val recency = mutableListOf<String>()

    val overlays: Map<String, T> get() = active

    fun markPresent(id: String, value: T) {
        recency.remove(id)
        recency.add(id)
        if (active.containsKey(id)) return
        active[id] = value
        while (recency.size > maxActive) {
            active.remove(recency.removeAt(0))
        }
    }

    fun markAbsent(id: String) {
        active.remove(id)
        recency.remove(id)
    }
}
