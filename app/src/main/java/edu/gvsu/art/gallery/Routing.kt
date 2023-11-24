package edu.gvsu.art.gallery

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavDestination

const val ART_GALLERY_WEB_URL = "https://artgallery.gvsu.edu"

object Route {
    const val ArtistDetail = "artists/{artist_id}"
    const val ArtworkDetail = "artworks/{artwork_id}"
    const val BrowseArtworkIndex = "browse/artworks"
    const val BrowseIndex = "browse"
    const val BrowseLocationDetail = "browse/locations/{location_id}?display_name={display_name}"
    const val BrowseLocationsIndex = "browse/locations"
    const val FavoritesIndex = "favorites"
    const val TourIndex = "tours"
    const val TourDetail = "tours/{tour_id}?display_name={display_name}"
    const val SearchIndex = "search"
    const val Settings = "browse/settings"
}

sealed class TabScreen(
    val route: String,
    @StringRes val title: Int,
    val icon: ImageVector,
) {
    data object Browse : TabScreen(
        route = Route.BrowseIndex,
        title = R.string.navigation_Browse,
        icon = Icons.Default.AutoStories
    )

    data object Tours : TabScreen(
        route = Route.TourIndex,
        title = R.string.navigation_Tours,
        icon = Icons.Default.Map,
    )

    data object Search : TabScreen(
        route = "search",
        title = R.string.navigation_Search,
        icon = Icons.Default.Search
    )

    data object Favorites : TabScreen(
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

fun NavController.navigateToArtistDetail(artistID: String) =
    navigate("artists/$artistID")

fun NavController.navigateToArtworkDetail(artworkID: String) =
    navigate("artworks/${artworkID}")

fun NavController.navigateToLocation(locationID: String, displayName: String) =
    navigate("browse/locations/$locationID?display_name=${displayName}")


fun NavController.navigateToTour(tourID: String, displayName: String) =
    navigate("tours/$tourID?display_name=${displayName}")
