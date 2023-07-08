package edu.gvsu.art.gallery.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import edu.gvsu.art.client.Location
import edu.gvsu.art.gallery.R
import edu.gvsu.art.gallery.lib.Async
import edu.gvsu.art.gallery.navigateToLocation

@Composable
fun LocationIndexScreen(navController: NavController) {
    val (data, retry) = useCampuses()

    fun navigateToLocation(campus: Location) {
        navController.navigateToLocation(
            locationID = campus.id,
            displayName = campus.name
        )
    }

    Column {
        GalleryTopAppBar(
            title = stringResource(id = R.string.navigation_Campuses),
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                }
            }
        )
        when (data) {
            is Async.Success ->
                CampusLoadedView(
                    campuses = data(),
                    onCampusClick = { navigateToLocation(it) }
                )
            is Async.Failure ->
                ErrorView(
                    error = data.error,
                    onRetryClick = { retry() }
                )
            else -> LoadingView()
        }
    }
}

@Composable
fun CampusLoadedView(
    campuses: List<Location>,
    onCampusClick: (campus: Location) -> Unit,
) {
    LazyColumn {
        items(campuses, key = { it.id }) { campus ->
            Box(modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                WideTitleCard(
                    title = campus.name,
                    imageURL = campus.mediaLargeURL,
                    onClick = { onCampusClick(campus) }
                )
            }
        }
    }
}
