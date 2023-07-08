package edu.gvsu.art.client.repository

import edu.gvsu.art.client.TourStop
import edu.gvsu.art.client.api.TourStopDetail
import edu.gvsu.art.client.common.optionalURL
import java.net.URL

val TourStopDetail.toDomainModel: TourStop
    get() =
        TourStop(
            artworkID = stop_objects_id!!.toString(),
            name = stop_name ?: "",
            media = parseStopMedia(stop_media),
            location = parseLocationGeoreference(stop_locations)
        )

private fun parseStopMedia(stopMedia: String?): URL? {
    stopMedia ?: return null
    val compactStopMedia = stopMedia.replace("\\s", "")
    val optionalURLs = compactStopMedia
        .split(";")
        .map { optionalURL(it) }

    return optionalURLs.find { it != null }
}
