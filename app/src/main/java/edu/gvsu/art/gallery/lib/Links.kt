package edu.gvsu.art.gallery.lib

import android.net.Uri
import java.net.URL


object Links {
    fun artworkDetail(id: String) =
        URL("https://artgallery.gvsu.edu/Detail/objects/${id}").toString()

    fun fromDetailLink(
        url: Uri,
        onArtwork: (artworkID: String) -> Unit,
    ) {
        if (isHTTP(url) && url.host == "artgallery.gvsu.edu") {
            val pathSegments = url.pathSegments
            if (pathSegments.isEmpty()) {
                return
            }
            val artworkID = pathSegments.last()
            onArtwork(artworkID)
        }
    }
}

private fun isHTTP(uri: Uri) =
    uri.scheme == "http" || uri.scheme == "https"
