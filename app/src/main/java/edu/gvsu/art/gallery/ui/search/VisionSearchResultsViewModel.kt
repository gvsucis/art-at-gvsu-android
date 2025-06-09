package edu.gvsu.art.gallery.ui.search

import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import edu.gvsu.art.client.api.VisionSearchClient
import edu.gvsu.art.gallery.Route


class VisionSearchResultsViewModel(
    handle: SavedStateHandle,
    private val client: VisionSearchClient
) : AndroidViewModel() {
    val uri = handle.toRoute<Route.VisionSearchResults>().imageUri.toUri()

    fun search() {
//        val inputStream = context.contentResolver.openInputStream(uri)
//        val imageData =
//            inputStream?.readBytes() ?: throw IllegalArgumentException("Cannot read image data")
//        val requestBody = imageData.toRequestBody("image/jpeg".toMediaTypeOrNull())
//        val multipart = MultipartBody.Part.createFormData("image", "image.jpg", requestBody)
    }
}
