package edu.gvsu.art.client.repository

import edu.gvsu.art.client.Artwork
import edu.gvsu.art.client.common.optionalURL
import java.net.URL

/**
 * Parses artworks from encoded
 * @param encodedObjects A string where artworks
 * are separated by semicolons and artwork attributes are
 * separated by a string-padded forward-slash. e.g.
 * "3817 / Dutchman with Canal Boat / https://artgallery.gvsu.edu/admin/media/collectiveaccess/images/1/5/6/2171_ca_object_representations_media_15698_small.jpg"
 * @return Partial Artwork containing an ID, name and thumbnail URL
 * Artwork(id="3817", name="Dutchman with Canal Boat", thumbnail=URL("https://artgallery...")
 */
fun parseRelatedObjects(encodedObjects: String?): List<Artwork> {
    encodedObjects ?: return listOf()

    return encodedObjects.split(ARTWORK_SEPARATOR)
        .mapNotNull { destructureRelatedWork(it.split(ARTWORK_ATTRIBUTES_SEPARATOR)) }
        .filter { it.thumbnail != null }
}


private fun destructureRelatedWork(workFields: List<String>): Artwork? {
    if (workFields.size != 3) { return null }

    val (id, name, thumbnail) = workFields
    return Artwork(
        id = id,
        name = name,
        thumbnail = optionalURL(parseThumbnail(thumbnail))
    )
}

private fun parseThumbnail(thumbnailURL: String): String? {
    return if (!thumbnailURL.startsWith("http")) {
        null
    } else if (thumbnailURL.endsWith(suffix = ",")) {
        thumbnailURL.dropLast(1)
    } else {
        thumbnailURL
    }
}

private const val ARTWORK_SEPARATOR = ";"
private const val ARTWORK_ATTRIBUTES_SEPARATOR = " / "
