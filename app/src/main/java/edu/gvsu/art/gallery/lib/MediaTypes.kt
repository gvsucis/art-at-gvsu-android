package edu.gvsu.art.gallery.lib

import android.webkit.MimeTypeMap
import java.net.URL

object MediaTypes {
    fun isVideo(url: URL): Boolean {
        val extension = MimeTypeMap.getFileExtensionFromUrl(url.toString())
        return knownVideoExtensions.contains(extension)
    }

    private val knownVideoExtensions = listOf(
        "m4v",
        "mp4",
    )
}
