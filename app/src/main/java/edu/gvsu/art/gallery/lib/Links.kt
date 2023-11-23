package edu.gvsu.art.gallery.lib

import android.net.Uri
import edu.gvsu.art.gallery.ART_GALLERY_WEB_URL
import java.net.URL


object Links {
    fun artworkDetail(id: String) =
        URL("${ART_GALLERY_WEB_URL}/Detail/objects/${id}").toString()

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
