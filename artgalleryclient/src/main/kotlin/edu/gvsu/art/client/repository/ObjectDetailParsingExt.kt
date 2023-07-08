package edu.gvsu.art.client.repository

import edu.gvsu.art.client.Artwork
import edu.gvsu.art.client.LatLng
import edu.gvsu.art.client.api.ObjectDetail
import edu.gvsu.art.client.splitOnPipes
import java.net.URL

internal fun ObjectDetail.parseMediaRepresentations(): List<URL> {
    if (media_reps.isNullOrBlank()) { return listOf() }

    return media_reps.splitOnPipes()
        .map { URL(it) }
}

internal fun ObjectDetail.parseLocationGeoreference(): LatLng? =
    parseLocationGeoreference(location_georeference)

internal fun ObjectDetail.parseRelatedWorks(): List<Artwork> =
    parseRelatedObjects(related_objects)
