package edu.gvsu.art.client.repository

import edu.gvsu.art.client.SecondaryMedia
import java.net.URL

fun parseSecondaryMedia(mediaRepresentations: String?, thumbnails: String?): List<SecondaryMedia> {
    if (mediaRepresentations.isNullOrBlank() || thumbnails.isNullOrBlank()) {
        return emptyList()
    }

    val mediaList = mediaRepresentations.split(";")
    val thumbnailList = thumbnails.split(";")

    if (mediaList.size != thumbnailList.size) return emptyList()

    try {
        return mediaList.zip(thumbnailList) { media, thumbnail ->
            SecondaryMedia(URL(media), URL(thumbnail))
        }
    } catch (_: Throwable) {
        return emptyList()
    }
}