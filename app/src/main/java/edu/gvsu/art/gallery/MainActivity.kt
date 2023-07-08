package edu.gvsu.art.gallery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
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
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import edu.gvsu.art.gallery.ui.*
import edu.gvsu.art.gallery.ui.theme.ArtAtGVSUTheme

@ExperimentalPagerApi
@ExperimentalAnimationApi
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
@ExperimentalAnimationApi
@ExperimentalPagerApi
@Composable
fun App() {
    ArtAtGVSUTheme {
        BottomNavigationView()
    }
}

@ExperimentalPermissionsApi
@ExperimentalComposeUiApi
@ExperimentalAnimationApi
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
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@ExperimentalPagerApi
fun NavGraphBuilder.routing(navController: NavController) {
    featuredGraph(navController)
    toursGraph(navController)
    searchGraph(navController)
    favoritesGraph(navController)
}

@ExperimentalPagerApi
@ExperimentalAnimationApi
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
    artworkDetailScreen(Route.FeaturedArtworkDetail, navController)
    artistDetailScreen(Route.FeaturedArtistDetail, navController)
    composable(Route.Settings) {
        SettingsScreen(navController)
    }
}

@ExperimentalComposeUiApi
@ExperimentalAnimationApi
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
    artworkDetailScreen(Route.TourArtworkDetail, navController)
    artistDetailScreen(Route.TourArtistDetail, navController)
}

@ExperimentalPermissionsApi
@ExperimentalAnimationApi
@ExperimentalPagerApi
@ExperimentalComposeUiApi
fun NavGraphBuilder.searchGraph(navController: NavController) {
    composable(Route.SearchIndex) {
        SearchIndexScreen(navController)
    }
    artworkDetailScreen(Route.SearchArtworkDetail, navController)
    artistDetailScreen(Route.SearchArtistDetail, navController)
}

@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@ExperimentalPagerApi
fun NavGraphBuilder.favoritesGraph(navController: NavController) {
    composable(TabScreen.Favorites.route) {
        FavoriteIndexScreen(navController)
    }
    artworkDetailScreen(Route.FavoritesArtworkDetail, navController)
    artistDetailScreen(Route.FavoritesArtistDetail, navController)
}

@ExperimentalPagerApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
fun NavGraphBuilder.artworkDetailScreen(route: String, navController: NavController) {
    composable(route) { backStackEntry ->
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

@Composable
fun FakeOutScreen(title: String) {
    Column {
        Text("Placeholder for ${title}")
    }
}
