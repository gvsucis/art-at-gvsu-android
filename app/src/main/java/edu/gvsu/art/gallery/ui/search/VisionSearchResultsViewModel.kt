package edu.gvsu.art.gallery.ui.search

import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import edu.gvsu.art.client.api.VisionSearchClient
import edu.gvsu.art.gallery.Route


class VisionSearchResultsViewModel(
    handle: SavedStateHandle,
    private val client: VisionSearchClient
) : ViewModel() {
    val uri = handle.toRoute<Route.VisionSearchResults>().imageUri.toUri()
}
