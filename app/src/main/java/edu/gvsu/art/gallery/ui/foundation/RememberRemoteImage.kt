package edu.gvsu.art.gallery.ui.foundation

import androidx.compose.runtime.Composable
import coil.compose.ImagePainter
import coil.compose.LocalImageLoader
import coil.request.ImageRequest
import java.net.URL

@Composable
inline fun rememberRemoteImage(
    url: URL?,
    onExecute: ImagePainter.ExecuteCallback = ImagePainter.ExecuteCallback.Default,
    builder: ImageRequest.Builder.() -> Unit = {},
): ImagePainter = coil.compose.rememberImagePainter(
    url.toString(),
    LocalImageLoader.current,
    onExecute,
    builder
)
