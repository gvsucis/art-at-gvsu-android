package edu.gvsu.art.gallery.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import edu.gvsu.art.client.Tour
import edu.gvsu.art.gallery.R
import edu.gvsu.art.gallery.lib.Async
import edu.gvsu.art.gallery.navigateToTour

@Composable
fun ToursIndexScreen(navController: NavController) {
    val (key, refreshKey) = useUniqueKey()
    val data = useTours(key)

    Scaffold(
        topBar = {
            GalleryTopAppBar(
                title = stringResource(id = R.string.navigation_Tours),
            )
        }
    ) { padding ->
        Box(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (data) {
                is Async.Success ->
                    TourIndexView(
                        navController = navController,
                        tours = data()
                    )

                is Async.Failure ->
                    ErrorView(
                        error = data.error,
                        onRetryClick = { refreshKey() }
                    )

                else -> LoadingView()
            }
        }
    }
}

@Composable
private fun TourIndexView(navController: NavController, tours: List<Tour>) {
    LazyColumn {
        items(tours, key = { it.id }) { tour ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                WideTitleCard(
                    title = tour.name,
                    imageURL = tour.mediaLarge,
                    onClick = {
                        navController.navigateToTour(
                            tourID = tour.id,
                            displayName = tour.name
                        )
                    }
                )
            }
        }
    }
}
