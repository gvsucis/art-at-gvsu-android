package edu.gvsu.art.client.repository

import edu.gvsu.art.client.Artwork
import edu.gvsu.art.client.LatLng
import edu.gvsu.art.client.common.asUrls
import edu.gvsu.art.client.common.optionalURL
import edu.gvsu.art.client.data.Artworks

internal val Artworks.toDomainModel: Artwork
    get() = Artwork(
        id = id,
        isPublic = is_public == 1L,
        mediaRepresentations = media_representations.asUrls(),
        secondaryMedia = parseSecondaryMedia(secondary_media_representations, secondary_media_representation_thumbnails),
        name = name ?: "",
        artistID = artist_id ?: "",
        artistName = artist_name ?: "",
        historicalContext = historical_context ?: "",
        workDescription = work_description ?: "",
        workDate = work_date ?: "",
        workMedium = work_medium ?: "",
        locationID = location_id.orEmpty(),
        location = location ?: "",
        identifier = identifier ?: "",
        creditLine = credit_line ?: "",
        locationGeoreference = LatLng.fromCoordinates(location_latitude, location_longitude),
        relatedWorks = parseRelatedObjects(related_objects),
        mediaSmall = optionalURL(media_small_url),
        mediaMedium = optionalURL(media_medium_url),
        mediaLarge = optionalURL(media_large_url),
        thumbnail = optionalURL(thumbnail_url),
        arDigitalAssetURL = optionalURL(ar_digital_asset_url)
    )
