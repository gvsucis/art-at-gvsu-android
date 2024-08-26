package edu.gvsu.art.gallery

import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.setThreadPolicy
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import edu.gvsu.art.gallery.ui.ArtistDetailScreen
import edu.gvsu.art.gallery.ui.FavoriteIndexScreen
import edu.gvsu.art.gallery.ui.LocationDetailScreen
import edu.gvsu.art.gallery.ui.LocationIndexScreen
import edu.gvsu.art.gallery.ui.SearchIndexScreen
import edu.gvsu.art.gallery.ui.SettingsScreen
import edu.gvsu.art.gallery.ui.TourDetailScreen
import edu.gvsu.art.gallery.ui.ToursIndexScreen
import edu.gvsu.art.gallery.ui.artwork.detail.ArtworkDetailScreen
import edu.gvsu.art.gallery.ui.browse.ArtworkCollectionScreen
import edu.gvsu.art.gallery.ui.browse.BrowseScreen
import edu.gvsu.art.gallery.ui.foundation.LocalTabScreen
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
    ArtGalleryTheme {
        BottomNavigationView()
    }
}

@ExperimentalPermissionsApi
@ExperimentalComposeUiApi
@Composable
fun BottomNavigationView() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val selectedTab = TabScreen.findSelected(currentDestination)

    CompositionLocalProvider(
        LocalTabScreen provides selectedTab
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                NavHost(
                    navController = navController,
                    startDestination = Route.BrowseIndex,
                    modifier = Modifier.weight(0.1f),
                ) {
                    routing(navController)
                }
                NavigationBar {
                    TabScreen.all.forEach { entry ->
                        val selected = entry == selectedTab

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
    composable(TabScreen.Browse.route) {
        BrowseScreen(navController = navController)
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
    composable(Route.BrowseCollection) {
        val tab = LocalTabScreen.current

        ArtworkCollectionScreen(
            onNavigateBack = {
                navController.navigateUp()
            },
            onNavigateToArtwork = { artworkID ->
                navController.navigateToArtworkDetail(tab, artworkID)
            }
        )
    }
    artworkDetailScreen(Route.FeaturedArtworkDetail, navController)
    artistDetailScreen(Route.FeaturedArtistDetail, navController)
    composable(Route.Settings) {
        SettingsScreen(navController)
    }
}

@ExperimentalComposeUiApi
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
    artworkDetailScreen(Route.TourArtworkDetail, navController)
    artistDetailScreen(Route.TourArtistDetail, navController)
}

@ExperimentalPermissionsApi
@ExperimentalComposeUiApi
fun NavGraphBuilder.searchGraph(navController: NavController) {
    composable(Route.SearchIndex) {
        SearchIndexScreen(navController)
    }
    artworkDetailScreen(Route.SearchArtworkDetail, navController)
    artistDetailScreen(Route.SearchArtistDetail, navController)
}

@ExperimentalComposeUiApi
fun NavGraphBuilder.favoritesGraph(navController: NavController) {
    composable(TabScreen.Favorites.route) {
        FavoriteIndexScreen(navController)
    }
    artworkDetailScreen(Route.FavoritesArtworkDetail, navController)
    artistDetailScreen(Route.FavoritesArtistDetail, navController)
}

@ExperimentalComposeUiApi
fun NavGraphBuilder.artworkDetailScreen(route: String, navController: NavController) {
    composable(route) {
        ArtworkDetailScreen(navController)
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
