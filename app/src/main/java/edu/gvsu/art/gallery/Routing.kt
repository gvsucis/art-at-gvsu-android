package edu.gvsu.art.gallery

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import edu.gvsu.art.client.ArtworkCollection
import kotlinx.serialization.Serializable

sealed class Route {
    @Serializable
    data object Browse : Route()

    @Serializable
    data object BrowseIndex : Route()

    @Serializable
    data class LocationDetail(val locationID: String, val displayName: String): Route()

    @Serializable
    data object Tours: Route()

    @Serializable
    data object Search: Route()

    @Serializable
    data object Favorites: Route()

    @Serializable
    data object ToursIndex : Route()

    @Serializable
    data object SearchIndex : Route()

    @Serializable
    data object FavoritesIndex : Route()

    @Serializable
    data object VisionSearch : Route()

    @Serializable
    data class VisionSearchResults(val imageUri: String) : Route()

    @Serializable
    data class SearchArtworkResults(val query: String) : Route()

    @Serializable
    data class SearchArtistResults(val query: String) : Route()
}

@Deprecated("Use safe-typed Route instead")
object Routing {
    const val BrowseCollection = "browse/collections/{collection_slug}"
    const val BrowseLocationsIndex = "browse/locations"
    const val FavoritesArtistDetail = "favorites/artists/{artist_id}"
    const val FavoritesArtworkDetail = "favorites/artworks/{artwork_id}"
    const val FeaturedArtistDetail = "browse/artists/{artist_id}"
    const val FeaturedArtworkDetail = "browse/artworks/{artwork_id}"
    const val TourDetail = "tours/{tour_id}?display_name={display_name}"
    const val TourArtistDetail = "tours/artists/{artist_id}"
    const val TourArtworkDetail = "tours/artworks/{artwork_id}"
    const val SearchArtistDetail = "search/artists/{artist_id}"
    const val SearchArtworkDetail = "search/artworks/{artwork_id}"
    const val Settings = "browse/settings"
}

sealed class TopLevelRoute(
    val route: Route,
    @StringRes val title: Int,
    val icon: ImageVector,
) {
    data object Browse : TopLevelRoute(
        route = Route.Browse,
        title = R.string.navigation_Browse,
        icon = Icons.Default.AutoStories
    )

    data object Tours : TopLevelRoute(
        route = Route.Tours,
        title = R.string.navigation_Tours,
        icon = Icons.Default.Map,
    )

    data object Search : TopLevelRoute(
        route = Route.Search,
        title = R.string.navigation_Search,
        icon = Icons.Default.Search
    )

    data object Favorites : TopLevelRoute(
        route = Route.Favorites,
        title = R.string.navigation_favorites,
        icon = Icons.Default.Favorite
    )

    companion object {
        val all = listOf(
            Browse,
            Tours,
            Search,
            Favorites,
        )
    }
}

fun NavController.navigateToArtistDetail(topLevelRoute: TopLevelRoute, artistID: String) =
    navigate("${topLevelRoute.route}/artists/$artistID")

fun NavController.navigateToArtworkDetail(topLevelRoute: TopLevelRoute, artworkID: String) =
    navigate("${topLevelRoute.route}/artworks/${artworkID}")

fun NavController.navigateToLocation(locationID: String, displayName: String) =
    navigate(Route.LocationDetail(locationID, displayName))

fun NavController.navigateToTour(tourID: String, displayName: String) =
    navigate("tours/$tourID?display_name=${displayName}")

fun NavController.navigateToCollection(collection: ArtworkCollection) =
    navigate("browse/collections/${collection.slug}")

internal class ArtworkCollectionArgs(val collection: ArtworkCollection) {
    constructor(savedStateHandle: SavedStateHandle) :
            this(ArtworkCollection.find(checkNotNull(savedStateHandle["collection_slug"]) as String)!!)
}

internal class ArtworkDetailArgs(val artworkID: String) {
    constructor(savedStateHandle: SavedStateHandle) :
            this(checkNotNull(savedStateHandle["artwork_id"]) as String)
}
