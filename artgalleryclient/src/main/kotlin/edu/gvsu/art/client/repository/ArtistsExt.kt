package edu.gvsu.art.client.repository

import edu.gvsu.art.client.Artist
import edu.gvsu.art.client.api.ArtistSearchResult
import edu.gvsu.art.client.data.Artists

val Artists.toDomainModel: Artist
    get() = Artist(
        id = id,
        isPublic = is_public == 1L,
        identifier = identifier ?: "",
        name = name ?: "",
        nationality = nationality ?: "",
        lifeDates = life_dates ?: "",
        biography = biography ?: "",
        relatedWorks = parseRelatedObjects(related_works)
    )
