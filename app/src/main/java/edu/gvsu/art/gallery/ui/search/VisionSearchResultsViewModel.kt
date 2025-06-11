package edu.gvsu.art.gallery.ui.search

import android.app.Application
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import edu.gvsu.art.client.api.VisionSearchClient
import edu.gvsu.art.client.api.visionsearch.ImageResult
import edu.gvsu.art.gallery.Route
import edu.gvsu.art.gallery.lib.Async
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class VisionSearchResultsViewModel(
    handle: SavedStateHandle,
    application: Application,
    private val client: VisionSearchClient,
) : AndroidViewModel(application) {
    val uri = handle.toRoute<Route.VisionSearchResults>().imageUri.toUri()

    val similarWorks = MutableStateFlow<Async<List<ImageResult>>>(Async.Uninitialized)

    init {
        fetchResults()
    }

    fun retry() = fetchResults()

    private fun fetchResults() {
        similarWorks.value = Async.Loading

        viewModelScope.launch(Dispatchers.IO) {
            similarWorks.value = search()
        }
    }

    private suspend fun search(): Async<List<ImageResult>> {
        try {
            val imageData = context.contentResolver.openInputStream(uri).use { inputStream ->
                inputStream?.readBytes()
                    ?: throw IllegalArgumentException("Cannot read image data")
            }
            val requestBody = imageData.toRequestBody("image/jpeg".toMediaType())
            val image = MultipartBody.Part.createFormData("image", "image.jpg", requestBody)
            val response = client.search(image = image)

            return Async.Success(response.results)
        } catch (e: Throwable) {
            return Async.Failure(error = e, value = emptyList())
        }
    }

    private val context
        get() = application.applicationContext
}
