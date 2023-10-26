package edu.gvsu.art.gallery.lib

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.ui.geometry.Rect
import kotlinx.parcelize.Parcelize
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

@Parcelize
class VideoPool(
    private val pool: ConcurrentHashMap<String, Long> = ConcurrentHashMap()
) : Parcelable {
    fun get(url: String): Long {
        return pool[url] ?: 1L
    }

    fun set(url: String, position: Long) {
        pool[url] = position
    }
}
