package edu.gvsu.art.gallery.ui.artwork.ar

import android.media.MediaPlayer
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.ar.core.AugmentedImage
import com.google.ar.core.AugmentedImage.TrackingMethod
import com.google.ar.core.Config
import com.google.ar.core.TrackingState
import edu.gvsu.art.client.Artwork
import edu.gvsu.art.gallery.lib.ARMediaCache
import edu.gvsu.art.gallery.ui.CloseIconButton
import io.github.sceneview.ar.ARSceneScope
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.arcore.addAugmentedImage
import io.github.sceneview.ar.arcore.getUpdatedAugmentedImages
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Size
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.NodeScope
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel
import java.io.File
import java.nio.ByteBuffer

/** Caps how many artwork overlays (video + model) stay live at once. */
private const val MAX_ACTIVE_OVERLAYS = 4

/**
 * Full-screen roaming AR experience: point the camera at any featured AR artwork
 * in the gallery and its video (and 3D model) plays in place.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ARExperienceScreen(
    onClose: () -> Unit,
    viewModel: ARExperienceViewModel = koinViewModel(),
) {
    val cameraPermission = rememberPermissionState(android.Manifest.permission.CAMERA)

    // Surface the system camera permission dialog as soon as the experience opens,
    // instead of gating it behind an in-app button.
    LaunchedEffect(Unit) {
        if (!cameraPermission.hasPermission) {
            cameraPermission.launchPermissionRequest()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        if (cameraPermission.hasPermission) {
            when (val state = viewModel.state) {
                ARExperienceViewModel.State.Loading ->
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Center),
                    )
                is ARExperienceViewModel.State.Ready ->
                    ARExperienceContent(state = state, mediaCache = viewModel.mediaCache)
                ARExperienceViewModel.State.Empty ->
                    Message("There's no AR artwork available right now.")
                ARExperienceViewModel.State.Failed ->
                    Message("Something went wrong loading AR artwork. Please try again.")
            }
        } else {
            Message("Camera access is needed to view artwork in augmented reality.")
        }

        Box(modifier = Modifier.statusBarsPadding()) {
            CloseIconButton(onClick = onClose)
        }
    }
}

@Composable
private fun ARExperienceContent(
    state: ARExperienceViewModel.State.Ready,
    mediaCache: ARMediaCache,
) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)

    // Images currently being overlaid, keyed by artwork id. Mutated only on add /
    // evict / removal so the per-frame session callback doesn't thrash recomposition.
    val tracked = remember { mutableStateMapOf<String, AugmentedImage>() }
    // Plain (non-snapshot) recency list driving least-recently-seen eviction.
    val recency = remember { mutableListOf<String>() }

    Box(modifier = Modifier.fillMaxSize()) {
        ARSceneView(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            planeRenderer = false,
            planeFindingMode = Config.PlaneFindingMode.DISABLED,
            focusMode = Config.FocusMode.AUTO,
            sessionConfiguration = { session, config ->
                // ARCore's environmental-HDR light estimation (SceneView's default) renders
                // models dark in dim galleries; disabling it falls back to SceneView's
                // constant default IBL so models stay evenly lit regardless of the room.
                config.lightEstimationMode = Config.LightEstimationMode.DISABLED
                state.referenceImages.forEach { reference ->
                    config.addAugmentedImage(
                        session,
                        reference.artwork.id,
                        reference.bitmap,
                        reference.widthMeters,
                    )
                }
            },
            onSessionUpdated = { _, frame ->
                frame.getUpdatedAugmentedImages().forEach { image ->
                    val name = image.name ?: return@forEach
                    if (!state.artworksById.containsKey(name)) return@forEach
                    // ARCore keeps an image's trackingState == TRACKING with trackingMethod
                    // == LAST_KNOWN_POSE after the visitor looks away (it remembers the world
                    // pose). Mirror iOS's `isTracked`: only FULL_TRACKING counts as present;
                    // anything else tears the overlay down so the video + model are released.
                    val present = image.trackingState == TrackingState.TRACKING &&
                        image.trackingMethod == TrackingMethod.FULL_TRACKING
                    if (present) {
                        recency.remove(name)
                        recency.add(name)
                        if (name !in tracked) {
                            tracked[name] = image
                            Log.d("ARExperience", "mount $name (${image.trackingMethod})")
                            while (recency.size > MAX_ACTIVE_OVERLAYS) {
                                tracked.remove(recency.removeAt(0))
                            }
                        }
                    } else {
                        if (tracked.remove(name) != null) {
                            Log.d("ARExperience", "unmount $name (${image.trackingState}/${image.trackingMethod})")
                        }
                        recency.remove(name)
                    }
                }
            },
        ) {
            tracked.forEach { (id, image) ->
                val artwork = state.artworksById[id] ?: return@forEach
                key(id) {
                    ArtworkAROverlay(
                        artwork = artwork,
                        augmentedImage = image,
                        mediaCache = mediaCache,
                        modelLoader = modelLoader,
                    )
                }
            }
        }

        Text(
            text = "Point your camera at the artwork and watch it come alive!",
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 48.dp),
        )
    }
}

/**
 * One detected artwork's overlay: a looping video plane plus an optional GLB model,
 * both anchored to the recognized image. The video pauses when tracking is lost and
 * resumes when the image is fully tracked again (mirrors iOS `ARArtworkEntity`).
 */
@Composable
private fun ARSceneScope.ArtworkAROverlay(
    artwork: Artwork,
    augmentedImage: AugmentedImage,
    mediaCache: ARMediaCache,
    modelLoader: ModelLoader,
) {
    var videoFile by remember { mutableStateOf<File?>(null) }
    var modelInstance by remember { mutableStateOf<ModelInstance?>(null) }
    var fullyTracking by remember { mutableStateOf(true) }

    LaunchedEffect(artwork.id) {
        artwork.arDigitalAssetURL?.let { videoFile = mediaCache.localFile(it.toString()) }

        artwork.arModelURL?.let { url ->
            mediaCache.localFile(url.toString())?.let { file ->
                // ModelLoader's String overload resolves through the AssetManager (the
                // APK's assets/), so a downloaded file path 404s. Read the bytes off the
                // main thread and hand the engine a buffer directly. A bad/partial model
                // shouldn't take down the whole session, so failures are swallowed.
                modelInstance = runCatching {
                    val bytes = withContext(Dispatchers.IO) { file.readBytes() }
                    val buffer = ByteBuffer.allocateDirect(bytes.size).apply {
                        put(bytes)
                        rewind()
                    }
                    modelLoader.createModelInstance(buffer)
                }.getOrNull()
            }
        }
    }

    AugmentedImageNode(
        augmentedImage = augmentedImage,
        onTrackingMethodChanged = { method ->
            fullyTracking = method == TrackingMethod.FULL_TRACKING
        },
    ) {
        videoFile?.let { file ->
            VideoPlane(file = file, image = augmentedImage, isPlaying = fullyTracking)
        }
        modelInstance?.let { instance ->
            // Every model is a refit GLB with its placement (scale, position, and the upright
            // -90X correction) baked into the file, so render with the authored transform and
            // apply nothing in-app. To tune a new model's placement, see the debug tuner in
            // ARDebugPlacement.kt / docs/ar-debug-placement.md.
            ModelNode(
                modelInstance = instance,
                autoAnimate = true,
                animationLoop = true,
            )
        }
    }
}

/**
 * A looping video laid flat on the recognized image. Owns the [MediaPlayer] and
 * releases it when the overlay leaves the composition (e.g. LRU eviction).
 */
@Composable
private fun NodeScope.VideoPlane(
    file: File,
    image: AugmentedImage,
    isPlaying: Boolean,
) {
    var prepared by remember { mutableStateOf(false) }
    val player = remember(file) {
        MediaPlayer().apply {
            setDataSource(file.path)
            isLooping = true
            setOnPreparedListener { prepared = true }
            prepareAsync()
        }
    }
    DisposableEffect(player) {
        onDispose { player.release() }
    }
    // Play only while the image is fully tracked, so audio doesn't keep running
    // when the visitor looks away.
    LaunchedEffect(prepared, isPlaying) {
        if (!prepared) return@LaunchedEffect
        if (isPlaying) {
            if (!player.isPlaying) player.start()
        } else if (player.isPlaying) {
            player.pause()
        }
    }

    // The image lies in the node's X-Z plane; rotate the (X-Y) video plane -90° about
    // X to lay it flat, sized to the printed image. NOTE: if the video appears rotated
    // or mirrored on device, adjust this rotation — it can't be verified off-device.
    val size = if (image.extentX > 0f && image.extentZ > 0f) {
        Size(x = image.extentX, y = image.extentZ, z = 0f)
    } else {
        null
    }
    VideoNode(
        player = player,
        size = size,
        rotation = Rotation(x = -90f),
    )
}

@Composable
private fun Message(text: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, color = Color.White)
    }
}
