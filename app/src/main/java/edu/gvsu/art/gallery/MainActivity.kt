package edu.gvsu.art.gallery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import edu.gvsu.art.gallery.ui.ArtistDetailScreen
import edu.gvsu.art.gallery.ui.ArtworkDetailScreen
import edu.gvsu.art.gallery.ui.BrowseScreen
import edu.gvsu.art.gallery.ui.FavoriteIndexScreen
import edu.gvsu.art.gallery.ui.FeaturedArtIndexScreen
import edu.gvsu.art.gallery.ui.LocationDetailScreen
import edu.gvsu.art.gallery.ui.LocationIndexScreen
import edu.gvsu.art.gallery.ui.SearchIndexScreen
import edu.gvsu.art.gallery.ui.SettingsScreen
import edu.gvsu.art.gallery.ui.TourDetailScreen
import edu.gvsu.art.gallery.ui.ToursIndexScreen
import edu.gvsu.art.gallery.ui.foundation.LocalTabScreen
import edu.gvsu.art.gallery.ui.theme.ArtAtGVSUTheme

@ExperimentalPagerApi
@ExperimentalComposeUiApi
@ExperimentalPermissionsApi
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}

@ExperimentalPermissionsApi
@ExperimentalComposeUiApi
@ExperimentalPagerApi
@Composable
fun App() {
    ArtAtGVSUTheme {
        BottomNavigationView()
    }
}

@ExperimentalPermissionsApi
@ExperimentalComposeUiApi
@ExperimentalPagerApi
@Composable
fun BottomNavigationView() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val selectedTab = TabScreen.findSelected(currentDestination)

    CompositionLocalProvider(
        LocalTabScreen provides selectedTab
    ) {
        Surface {
            Scaffold(
                bottomBar = {
                    BottomNavigation(
                        backgroundColor = MaterialTheme.colors.surface
                    ) {
                        TabScreen.all.forEach { entry ->
                            val selected = entry == selectedTab
                            val itemColor = if (selected) {
                                MaterialTheme.colors.primary
                            } else {
                                LocalContentColor.current.copy(alpha = 0.6f)
                            }

                            BottomNavigationItem(
                                icon = {
                                    Icon(
                                        entry.icon,
                                        tint = itemColor,
                                        contentDescription = null
                                    )
                                },
                                label = {
                                    Text(
                                        stringResource(entry.title),
                                        color = itemColor
                                    )
                                },
                                selected = selected,
                                onClick = {
                                    navController.navigate(entry.route) {
                                        popUpTo(entry.route) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = Route.BrowseIndex,
                    modifier = Modifier.padding(innerPadding),
                ) {
                    routing(navController)
                }
            }
        }
    }
}

@ExperimentalPermissionsApi
@ExperimentalComposeUiApi
@ExperimentalPagerApi
fun NavGraphBuilder.routing(navController: NavController) {
    featuredGraph(navController)
    toursGraph(navController)
    searchGraph(navController)
    favoritesGraph(navController)
}

@ExperimentalPagerApi
@ExperimentalComposeUiApi
fun NavGraphBuilder.featuredGraph(navController: NavController) {
    composable(TabScreen.Browse.route) {
        BrowseScreen(navController)
    }
    composable(Route.BrowseLocationsIndex) {
        LocationIndexScreen(navController)
    }
    composable(Route.BrowseLocationDetail) { backStackEntry ->
        LocationDetailScreen(
            navController,
            locationID = backStackEntry.arguments?.getString("location_id"),
            locationName = backStackEntry.arguments?.getString("display_name") ?: ""
        )
    }
    composable(Route.BrowseArtworkIndex) {
        FeaturedArtIndexScreen(navController)
    }
    artworkDetailScreen(navController)
    artistDetailScreen(Route.FeaturedArtistDetail, navController)
    composable(Route.Settings) {
        SettingsScreen(navController)
    }
}

@ExperimentalComposeUiApi
@ExperimentalPagerApi
fun NavGraphBuilder.toursGraph(navController: NavController) {
    composable(TabScreen.Tours.route) {
        ToursIndexScreen(navController)
    }
    composable(Route.TourDetail) { backStackEntry ->
        TourDetailScreen(
            navController = navController,
            tourID = backStackEntry.arguments?.getString("tour_id"),
            tourName = backStackEntry.arguments?.getString("display_name") ?: ""
        )
    }
    artistDetailScreen(Route.TourArtistDetail, navController)
}

@ExperimentalPermissionsApi
@ExperimentalPagerApi
@ExperimentalComposeUiApi
fun NavGraphBuilder.searchGraph(navController: NavController) {
    composable(Route.SearchIndex) {
        SearchIndexScreen(navController)
    }
    artistDetailScreen(Route.SearchArtistDetail, navController)
}

@ExperimentalComposeUiApi
@ExperimentalPagerApi
fun NavGraphBuilder.favoritesGraph(navController: NavController) {
    composable(TabScreen.Favorites.route) {
        FavoriteIndexScreen(navController)
    }
    artistDetailScreen(Route.FavoritesArtistDetail, navController)
}

@ExperimentalPagerApi
@ExperimentalComposeUiApi
fun NavGraphBuilder.artworkDetailScreen(navController: NavController) {
    composable(
        Route.ArtworkDetail,
        deepLinks = listOf(navDeepLink { uriPattern = "$ART_GALLERY_WEB_URL/Detail/objects/{artwork_id}" })
    ) { backStackEntry ->
        ArtworkDetailScreen(
            navController,
            backStackEntry.arguments?.getString("artwork_id")
        )
    }
}

fun NavGraphBuilder.artistDetailScreen(route: String, navController: NavController) {
    composable(route) { backStackEntry ->
        ArtistDetailScreen(
            navController,
            backStackEntry.arguments?.getString("artist_id")
        )
    }
}
