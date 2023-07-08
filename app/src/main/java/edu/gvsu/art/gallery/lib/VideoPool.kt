package edu.gvsu.art.gallery.lib

import androidx.compose.ui.geometry.Rect
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

object VideoPool {
    const val DEBOUNCE_DELAY = 500L

    private val pool = ConcurrentHashMap<String, Long>()

    fun get(url: String): Long {
        return pool[url] ?: 1L
    }

    fun set(url: String, position: Long) {
        pool[url] = position
    }

    private val positionPool = ConcurrentHashMap<String, Rect>()

    fun setRect(videoKey: String, rect: Rect) {
        if (rect.height <= 0) {
            removeRect(videoKey)
        } else {
            positionPool[videoKey] = rect
        }
    }

    fun removeRect(url: String) {
        positionPool.remove(url)
    }

    fun fullInScreen(videoKey: String, videoHeight: Int): Boolean {
        positionPool[videoKey]?.let {
            return videoHeight == it.height.toInt()
        }
        return false
    }

    fun isMostCenter(videoKey: String, middle: Float): Boolean {
        if (positionPool.size == 0) {
            return false
        }
        if (positionPool.size == 1) {
            return true
        }
        var centerUrl = videoKey
        var minGap = Float.MAX_VALUE
        positionPool.forEach {
            abs((it.value.top + it.value.bottom) / 2 - middle).let { curGap ->
                if (curGap < minGap) {
                    minGap = curGap
                    centerUrl = it.key
                }
            }
        }
        return videoKey == centerUrl
    }
}
