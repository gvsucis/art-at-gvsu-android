package edu.gvsu.art.gallery.ui.artwork.detail

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import edu.gvsu.art.client.Artwork
import edu.gvsu.art.gallery.lib.Async
import edu.gvsu.art.gallery.lib.FileDownloader
import edu.gvsu.art.gallery.lib.arAssetsDirectory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class ARAsset(
    val progress: Float = 0f,
    val value: Async<Uri> = Async.Uninitialized,
) {
    val isLoading: Boolean
        get() = value is Async.Loading
}

data class ARAssetState(
    val value: ARAsset,
    val requestARAsset: () -> Unit = {}
)

@Composable
fun rememberARAsset(artwork: Artwork, onComplete: (uri: Uri) -> Unit): ARAssetState {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val (arAsset, setARAsset) = remember(artwork.id) {
        mutableStateOf(ARAsset())
    }

    fun fetch() {
        setARAsset(ARAsset(value = Async.Loading))

        scope.launch(Dispatchers.IO) {
            val result = FileDownloader.download(
                context,
                url = artwork.arDigitalAssetURL!!.toString(),
                onProgressUpdate = { progress ->
                    setARAsset(ARAsset(value = Async.Loading, progress = progress))
                },
                directory = context.arAssetsDirectory()
            ).fold(
                onSuccess = { Async.Success(it) },
                onFailure = { Async.Failure(it) }
            )

            setARAsset(ARAsset(value = result))

            if (result is Async.Success) {
                onComplete(result.invoke())
            }
        }
    }

    return ARAssetState(
        value = arAsset,
        requestARAsset = ::fetch
    )
}
