package edu.gvsu.art.client.repository

import edu.gvsu.art.client.Tour
import edu.gvsu.art.client.api.TourSearchResult
import edu.gvsu.art.client.common.optionalURL

val TourSearchResult.toDomainModel: List<Tour>
    get() =
        tourSearchDetails.map {
            Tour(
                id = it.tour_id!!.toString(),
                name = it.tour_name ?: "",
                mediaLarge = optionalURL(it.media_large_url)
            )
        }
