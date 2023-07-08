package edu.gvsu.art.gallery.ui

import androidx.compose.runtime.Composable
import edu.gvsu.art.client.repository.CampusRepository
import edu.gvsu.art.client.repository.LocationRepository

@Composable
fun useLocation(locationID: String) = useKeyedRepositoryResource(
    fetch = {
        get<LocationRepository>().find(locationID = locationID)
    }
)

@Composable
fun useCampuses() = useKeyedRepositoryResource(
    fetch = {
        get<CampusRepository>().all()
    }
)
