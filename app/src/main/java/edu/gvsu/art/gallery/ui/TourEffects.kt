package edu.gvsu.art.gallery.ui

import androidx.compose.runtime.Composable
import edu.gvsu.art.client.Tour
import edu.gvsu.art.client.repository.TourRepository
import edu.gvsu.art.gallery.lib.Async

@Composable
fun useTours(key: Any = Unit): Async<List<Tour>> = useRepositoryResource(
    fetch = {
        get<TourRepository>().all()
    },
    key = key
)

@Composable
fun useTour(tourID: String) = useKeyedRepositoryResource(
    fetch = {
        get<TourRepository>().find(tourID)
    }
)
