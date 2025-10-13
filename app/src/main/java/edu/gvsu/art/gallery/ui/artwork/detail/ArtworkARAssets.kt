package edu.gvsu.art.gallery.ui.artwork.detail

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import edu.gvsu.art.client.Artwork
import edu.gvsu.art.gallery.lib.Async
import edu.gvsu.art.gallery.lib.FileDownloader
import edu.gvsu.art.gallery.lib.arAssetsDirectory
import edu.gvsu.art.gallery.lib.fileURI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class ArtworkARAssets(
    val imageURL: Uri,
    val videoURL: Uri,
)

data class ARAssetState(
    val value: Async<ArtworkARAssets>,
    val progress: Float = 0f,
    val requestARAsset: () -> Unit = {}
)

@Composable
fun rememberARAsset(artwork: Artwork, onComplete: (uris: ArtworkARAssets) -> Unit): ARAssetState {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val (arAsset, setARAsset) = remember(artwork.id) {
        mutableStateOf<Async<ArtworkARAssets>>(Async.Uninitialized)
    }
    val (progress, setProgress) = remember(artwork.id) {
        mutableFloatStateOf(0f)
    }

    fun fetch() {
        setARAsset(Async.Loading)

        scope.launch(Dispatchers.IO) {
            val videoResult = FileDownloader.download(
                url = artwork.arDigitalAssetURL!!.toString(),
                onProgressUpdate = { progress ->
                    setProgress(progress)
                },
                directory = context.arAssetsDirectory()
            ).fold(
                onSuccess = { Async.Success(it) },
                onFailure = { Async.Failure(it) }
            )

            val imageResult = FileDownloader.download(
                url = artwork.mediaMedium!!.toString(),
                directory = context.arAssetsDirectory()
            ).fold(
                onSuccess = { Async.Success(it) },
                onFailure = { Async.Failure(it) }
            )

            if (videoResult is Async.Success && imageResult is Async.Success) {
                val result = ArtworkARAssets(
                    videoURL = context.fileURI(videoResult.invoke()),
                    imageURL = context.fileURI(imageResult.invoke())
                )

                setARAsset(Async.Success(result))
                onComplete(result)
            } else {
                setARAsset(Async.Failure(error = Throwable("Failed to load media")))
            }
        }
    }

    return ARAssetState(
        value = arAsset,
        progress = progress,
        requestARAsset = ::fetch
    )
}
