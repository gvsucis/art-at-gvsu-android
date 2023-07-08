package edu.gvsu.art.client.repository

import edu.gvsu.art.client.Artwork
import edu.gvsu.art.client.Location
import edu.gvsu.art.client.api.LocationDetail

internal fun LocationDetail.parseChildLocations(): List<Location> {
    val idsString = child_location_id ?: return emptyList()
    val namesString = child_location_name ?: return emptyList()

    if (idsString.isBlank() || namesString.isBlank()) {
        return emptyList()
    }

    val ids = idsString.split(";")
    val names = namesString.split(";")

    if (ids.size != names.size) {
        return emptyList()
    }

    return ids.zip(names).map { (id, name) ->
        Location(id = id, name = name)
    }
}

internal fun LocationDetail.parseRelatedObjects(): List<Artwork> {
    return parseRelatedObjects(encodedObjects = related_objects)
}
