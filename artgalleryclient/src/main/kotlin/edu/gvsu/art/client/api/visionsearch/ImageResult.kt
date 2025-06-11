package edu.gvsu.art.client.api.visionsearch

import java.net.URL

data class ImageResult(
    var object_id: String = "",
    var image_url: String = ""
) {
    val id: String
        get() = object_id

    val imageURL: String
        get() = image_url
}
