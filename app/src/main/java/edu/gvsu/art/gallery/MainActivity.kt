package edu.gvsu.art.gallery

import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.setThreadPolicy
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import edu.gvsu.art.gallery.ui.ArtistDetailScreen
import edu.gvsu.art.gallery.ui.ArtworkMediaDialog
import edu.gvsu.art.gallery.ui.LocationDetailScreen
import edu.gvsu.art.gallery.ui.LocationIndexScreen
import edu.gvsu.art.gallery.ui.SettingsScreen
import edu.gvsu.art.gallery.ui.TourDetailScreen
import edu.gvsu.art.gallery.ui.ToursIndexScreen
import edu.gvsu.art.gallery.ui.artwork.detail.ArtworkDetailScreen
import edu.gvsu.art.gallery.ui.browse.ArtworkCollectionScreen
import edu.gvsu.art.gallery.ui.browse.BrowseScreen
import edu.gvsu.art.gallery.ui.favorites.FavoriteIndexScreen
import edu.gvsu.art.gallery.ui.foundation.LocalTopLevelRoute
import edu.gvsu.art.gallery.ui.mediaviewer.LocalMediaViewerState
import edu.gvsu.art.gallery.ui.mediaviewer.rememberMediaViewerState
import edu.gvsu.art.gallery.ui.search.SearchArtistResultsScreen
import edu.gvsu.art.gallery.ui.search.SearchArtworkResultsScreen
import edu.gvsu.art.gallery.ui.search.SearchIndexScreen
import edu.gvsu.art.gallery.ui.search.VisionSearchResultsScreen
import edu.gvsu.art.gallery.ui.theme.ArtGalleryTheme

@ExperimentalComposeUiApi
@ExperimentalPermissionsApi
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableStrictModeOnDebug()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
    }
}

@ExperimentalPermissionsApi
@ExperimentalComposeUiApi
@Composable
fun App() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val selectedTab = TopLevelRoute.all.find { entry ->
        currentDestination?.hierarchy?.any {
            it.hasRoute(
                entry.route::class
            )
        } == true
    } ?: TopLevelRoute.Browse

    val mediaViewerState = rememberMediaViewerState()

    ArtGalleryTheme {
        CompositionLocalProvider(
            LocalMediaViewerState provides mediaViewerState,
            LocalTopLevelRoute provides selectedTab
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = colorScheme.background
            ) {
                Box(Modifier.fillMaxSize()) {
                    LaunchedEffect(currentDestination) {
                        Log.d(
                            "hierarchy",
                            currentDestination?.hierarchy?.map { it.route.toString() }
                                ?.joinToString("\n").orEmpty()
                        )
                    }
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = Route.Browse,
                            modifier = Modifier.weight(0.1f),
                        ) {
                            routing(navController)
                        }
                        NavigationBar {
                            TopLevelRoute.all.forEach { entry ->
                                NavigationBarItem(
                                    icon = {
                                        Icon(
                                            entry.icon,
                                            contentDescription = null
                                        )
                                    },
                                    label = {
                                        Text(
                                            stringResource(entry.title),
                                        )
                                    },
                                    selected = currentDestination?.hierarchy?.any {
                                        it.hasRoute(
                                            entry.route::class
                                        )
                                    } == true,
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

                    ArtworkMediaDialog()
                }
            }
        }
    }
}

@ExperimentalPermissionsApi
@ExperimentalComposeUiApi
fun NavGraphBuilder.routing(navController: NavController) {
    featuredGraph(navController)
    toursGraph(navController)
    searchGraph(navController)
    favoritesGraph(navController)
}

@ExperimentalComposeUiApi
fun NavGraphBuilder.featuredGraph(navController: NavController) {
    navigation<Route.Browse>(startDestination = Route.BrowseIndex) {
        composable<Route.BrowseIndex> {
            BrowseScreen(navController = navController)
        }
        composable(Routing.BrowseLocationsIndex) {
            LocationIndexScreen(navController)
        }
        composable(Routing.BrowseCollection) {
            val tab = LocalTopLevelRoute.current

            ArtworkCollectionScreen(
                onNavigateBack = {
                    navController.navigateUp()
                },
                onNavigateToArtwork = { artworkID ->
                    navController.navigateToArtworkDetail(tab, artworkID)
                }
            )
        }
        artworkScreens(Routing.FeaturedArtworkDetail, navController)
        artistDetailScreen(Routing.FeaturedArtistDetail, navController)
        composable(Routing.Settings) {
            SettingsScreen(navController)
        }
    }
}

@ExperimentalComposeUiApi
fun NavGraphBuilder.toursGraph(navController: NavController) {
    navigation<Route.Tours>(startDestination = Route.ToursIndex) {
        composable<Route.ToursIndex> {
            ToursIndexScreen(navController)
        }
        composable(Routing.TourDetail) { backStackEntry ->
            TourDetailScreen(
                navController = navController,
                tourID = backStackEntry.arguments?.getString("tour_id"),
                tourName = backStackEntry.arguments?.getString("display_name") ?: ""
            )
        }
        artworkScreens(Routing.TourArtworkDetail, navController)
        artistDetailScreen(Routing.TourArtistDetail, navController)
    }
}

@ExperimentalPermissionsApi
@ExperimentalComposeUiApi
fun NavGraphBuilder.searchGraph(navController: NavController) {
    navigation<Route.Search>(startDestination = Route.SearchIndex) {
        composable<Route.SearchIndex> {
            SearchIndexScreen(navController)
        }
        composable<Route.VisionSearch> {
            SearchIndexScreen(navController)
        }
        composable<Route.VisionSearchResults> {
            VisionSearchResultsScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToArtwork = { artworkID ->
                    navController.navigateToArtworkDetail(
                        TopLevelRoute.Search,
                        artworkID
                    )
                }
            )
        }
        composable<Route.SearchArtworkResults> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.SearchArtworkResults>()
            SearchArtworkResultsScreen(
                query = route.query,
                onNavigateBack = { navController.navigateUp() },
                onNavigateToArtwork = { artworkID ->
                    navController.navigateToArtworkDetail(
                        TopLevelRoute.Search,
                        artworkID
                    )
                }
            )
        }
        composable<Route.SearchArtistResults> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.SearchArtistResults>()
            SearchArtistResultsScreen(
                query = route.query,
                onNavigateBack = { navController.navigateUp() },
                onNavigateToArtist = { artistID ->
                    navController.navigateToArtistDetail(
                        TopLevelRoute.Search,
                        artistID
                    )
                }
            )
        }
        artworkScreens(Routing.SearchArtworkDetail, navController)
        artistDetailScreen(Routing.SearchArtistDetail, navController)
    }
}

@ExperimentalComposeUiApi
fun NavGraphBuilder.favoritesGraph(navController: NavController) {
    navigation<Route.Favorites>(startDestination = Route.FavoritesIndex) {
        composable<Route.FavoritesIndex> {
            FavoriteIndexScreen(navController = navController)
        }
        artworkScreens(Routing.FavoritesArtworkDetail, navController)
        artistDetailScreen(Routing.FavoritesArtistDetail, navController)
    }
}

fun NavGraphBuilder.locationDetailScreen(navController: NavController) {
    composable<Route.LocationDetail> { backStackEntry ->
        val route = backStackEntry.toRoute<Route.LocationDetail>()

        LocationDetailScreen(
            navController,
            locationID = route.locationID,
            locationName = route.displayName,
        )
    }
}

@ExperimentalComposeUiApi
fun NavGraphBuilder.artworkScreens(route: String, navController: NavController) {
    composable(route) {
        ArtworkDetailScreen(navController)
    }
    locationDetailScreen(navController)
}

fun NavGraphBuilder.artistDetailScreen(route: String, navController: NavController) {
    composable(route) { backStackEntry ->
        ArtistDetailScreen(
            navController,
            backStackEntry.arguments?.getString("artist_id")
        )
    }
}


private fun enableStrictModeOnDebug() {
    if (BuildConfig.DEBUG) {
        setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectNetwork()
                .penaltyDeath()
                .build()
        )
    }
}
