package edu.gvsu.art.client.repository

import edu.gvsu.art.client.Artwork
import edu.gvsu.art.client.LatLng
import edu.gvsu.art.client.common.asUrls
import edu.gvsu.art.client.common.optionalURL

fun artworkMapper(
    id: String,
    isPublic: Long?,
    mediaRepresentations: String?,
    name: String?,
    artistID: String?,
    artistName: String?,
    historicalContext: String?,
    workDescription: String?,
    workDate: String?,
    workMedium: String?,
    location: String?,
    identifier: String?,
    creditLine: String?,
    locationLatitude: Double?,
    locationLongitude: Double?,
    relatedObjects: String?,
    mediaSmallURL: String?,
    mediaMediumURL: String?,
    mediaLargeURL: String?,
    thumbnailURL: String?,
    createdAt: String,
    arDigitalAssetURL: String?,
    locationID: String?,
) = Artwork(
    id = id,
    isPublic = isPublic == 1L,
    mediaRepresentations = mediaRepresentations.asUrls(),
    name = name ?: "",
    artistID = artistID ?: "",
    artistName = artistName ?: "",
    historicalContext = historicalContext ?: "",
    workDescription = workDescription ?: "",
    workDate = workDate ?: "",
    workMedium = workMedium ?: "",
    locationID = locationID.orEmpty(),
    location = location ?: "",
    identifier = identifier ?: "",
    creditLine = creditLine ?: "",
    locationGeoreference = LatLng.fromCoordinates(locationLatitude, locationLongitude),
    relatedWorks = parseRelatedObjects(relatedObjects),
    mediaSmall = optionalURL(mediaSmallURL),
    mediaMedium = optionalURL(mediaMediumURL),
    mediaLarge = optionalURL(mediaLargeURL),
    thumbnail = optionalURL(thumbnailURL),
    arDigitalAssetURL = optionalURL(arDigitalAssetURL)
)
