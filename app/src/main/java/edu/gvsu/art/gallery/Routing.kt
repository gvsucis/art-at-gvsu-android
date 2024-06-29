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
import androidx.navigation.NavDestination
import edu.gvsu.art.client.ArtworkCollection

object Route {
    const val BrowseCollection = "browse/collections/{collection_slug}"
    const val BrowseIndex = "browse"
    const val BrowseLocationDetail = "browse/locations/{location_id}?display_name={display_name}"
    const val BrowseLocationsIndex = "browse/locations"
    const val FavoritesArtistDetail = "favorites/artists/{artist_id}"
    const val FavoritesArtworkDetail = "favorites/artworks/{artwork_id}"
    const val FavoritesIndex = "favorites"
    const val FeaturedArtistDetail = "browse/artists/{artist_id}"
    const val FeaturedArtworkDetail = "browse/artworks/{artwork_id}"
    const val TourIndex = "tours"
    const val TourDetail = "tours/{tour_id}?display_name={display_name}"
    const val TourArtistDetail = "tours/artists/{artist_id}"
    const val TourArtworkDetail = "tours/artworks/{artwork_id}"
    const val SearchIndex = "search"
    const val SearchArtistDetail = "search/artists/{artist_id}"
    const val SearchArtworkDetail = "search/artworks/{artwork_id}"
    const val Settings = "browse/settings"
}

sealed class TabScreen(
    val route: String,
    @StringRes val title: Int,
    val icon: ImageVector,
) {
    object Browse : TabScreen(
        route = Route.BrowseIndex,
        title = R.string.navigation_Browse,
        icon = Icons.Default.AutoStories
    )

    object Tours : TabScreen(
        route = Route.TourIndex,
        title = R.string.navigation_Tours,
        icon = Icons.Default.Map,
    )

    object Search : TabScreen(
        route = "search",
        title = R.string.navigation_Search,
        icon = Icons.Default.Search
    )

    object Favorites : TabScreen(
        route = Route.FavoritesIndex,
        title = R.string.navigation_Favorites,
        icon = Icons.Default.Favorite
    )

    companion object {
        val all = listOf(
            Browse,
            Tours,
            Search,
            Favorites,
        )

        fun findSelected(navDestination: NavDestination?): TabScreen {
            val route = navDestination?.route
            route?.isNotBlank() ?: return Browse

            val tabRoute = route.split("/").first()
            return fromRoute(tabRoute)
        }

        private fun fromRoute(route: String): TabScreen {
            return mapOf(
                Browse.route to Browse,
                Tours.route to Tours,
                Search.route to Search,
                Favorites.route to Favorites
            ).getOrDefault(route, Browse)
        }
    }
}

fun NavController.navigateToArtistDetail(tabScreen: TabScreen, artistID: String) =
    navigate("${tabScreen.route}/artists/$artistID")

fun NavController.navigateToArtworkDetail(tabScreen: TabScreen, artworkID: String) =
    navigate("${tabScreen.route}/artworks/${artworkID}")

fun NavController.navigateToLocation(locationID: String, displayName: String) =
    navigate("browse/locations/$locationID?display_name=${displayName}")


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
