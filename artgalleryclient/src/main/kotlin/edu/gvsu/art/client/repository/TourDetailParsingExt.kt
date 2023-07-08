package edu.gvsu.art.client.repository

import edu.gvsu.art.client.Tour
import edu.gvsu.art.client.TourStop
import edu.gvsu.art.client.api.TourDetail
import edu.gvsu.art.client.common.optionalURL

internal val TourDetail.toDomainModel: Tour
    get() {
        return Tour(
            id = tour_id!!.toString(),
            name = tour_name ?: "",
            description = tour_description ?: "",
            mediaIcon = optionalURL(media_icon),
            mediaLarge = optionalURL(media_large_url),
            mediaMedium = optionalURL(media_medium_url),
            mediaSmall = optionalURL(media_small_url),
            stops = parseTourStops(),
        )
    }

fun TourDetail.parseTourStops(): List<TourStop> {
    val idsString = tour_stops_id ?: return emptyList()
    val namesString = tour_stops_name ?: return emptyList()

    if (idsString.isBlank() || namesString.isBlank()) {
        return emptyList()
    }

    val ids = idsString.split(";").filter { it.isNotBlank() }
    val names = namesString.split(";").filter { it.isNotBlank() }

    if (ids.size != names.size) {
        return emptyList()
    }

    return ids.zip(names).map { (id, name) ->
        TourStop(
            id = id,
            name = name
        )
    }
}

