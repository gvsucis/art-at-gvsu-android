package edu.gvsu.art.gallery.ui.artwork.ar

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import edu.gvsu.art.client.Artwork
import edu.gvsu.art.client.ArtworkCollection
import edu.gvsu.art.client.repository.ArtworkSearchRepository
import edu.gvsu.art.gallery.lib.ARMediaCache
import edu.gvsu.art.gallery.lib.FileDownloader
import edu.gvsu.art.gallery.lib.arAssetsDirectory
import edu.gvsu.art.gallery.lib.arReferencesDirectory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ARExperienceViewModel(
    application: Application,
    private val searchRepository: ArtworkSearchRepository,
) : AndroidViewModel(application) {
    sealed interface State {
        data object Loading : State
        data class Ready(
            val referenceImages: List<ReferenceImage>,
            val artworksById: Map<String, Artwork>,
        ) : State
        data object Empty : State
        data object Failed : State
    }

    var state by mutableStateOf<State>(State.Loading)
        private set

    val mediaCache = ARMediaCache(
        directory = application.arAssetsDirectory(),
        scope = viewModelScope,
    )

    private val referencesDirectory = application.arReferencesDirectory()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            val artworks = searchRepository.searchCollection(ArtworkCollection.FeaturedAR)
                .getOrNull()
                ?.filter { it.hasAR }

            val next = when {
                artworks == null -> State.Failed
                artworks.isEmpty() -> State.Empty
                else -> {
                    val references = artworks.mapNotNull { buildReferenceImage(it) }
                    if (references.isEmpty()) {
                        State.Failed
                    } else {
                        State.Ready(
                            referenceImages = references,
                            artworksById = references.associate { it.artwork.id to it.artwork },
                        )
                    }
                }
            }

            withContext(Dispatchers.Main) { state = next }
        }
    }

    private suspend fun buildReferenceImage(artwork: Artwork): ReferenceImage? {
        val imageURL = artwork.mediaMedium ?: artwork.mediaLarge ?: return null
        val file = FileDownloader.download(imageURL.toString(), referencesDirectory).getOrNull() ?: return null
        val decoded = BitmapFactory.decodeFile(file.path) ?: return null
        // ARCore's augmented image database requires ARGB_8888 input.
        val bitmap = if (decoded.config == Bitmap.Config.ARGB_8888) {
            decoded
        } else {
            val converted = decoded.copy(Bitmap.Config.ARGB_8888, false)
            decoded.recycle()
            converted ?: return null
        }
        return ReferenceImage(
            artwork = artwork,
            bitmap = bitmap,
            widthMeters = bitmap.width * PIXELS_TO_METERS,
        )
    }

    companion object {
        /**
         * Pixels-to-meters at 96 dpi. The search API doesn't expose real artwork
         * dimensions, so we approximate the printed width from the image (matches iOS).
         */
        private const val PIXELS_TO_METERS = 0.0002645833f
    }

    data class ReferenceImage(
        val artwork: Artwork,
        val bitmap: Bitmap,
        val widthMeters: Float,
    )
}
