package edu.gvsu.art.gallery.ui.artwork.detail

import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentOnAttachListener
import androidx.lifecycle.lifecycleScope
import coil.imageLoader
import coil.request.ImageRequest
import com.google.android.filament.filamat.MaterialBuilder
import com.google.ar.core.AugmentedImage
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Sceneform
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.EngineInstance
import com.google.ar.sceneform.rendering.ExternalTexture
import com.google.ar.sceneform.rendering.Material
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.BaseArFragment
import com.google.ar.sceneform.ux.TransformableNode
import edu.gvsu.art.client.Artwork
import edu.gvsu.art.gallery.R
import edu.gvsu.art.gallery.lib.ARVideoCache
import edu.gvsu.art.gallery.lib.Async
import edu.gvsu.art.gallery.ui.CloseIconButton
import edu.gvsu.art.gallery.ui.theme.ArtGalleryTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.nio.ByteBuffer
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

private const val TAG = "gv.ar"

class ArtworkARActivity : FragmentActivity(), FragmentOnAttachListener,
    BaseArFragment.OnSessionConfigurationListener {

    companion object {
        private const val EXTRA_ARTWORK_ID = "EXTRA_ARTWORK_ID"

        fun start(context: Context, artworkId: String? = null) {
            Intent(context, ArtworkARActivity::class.java).apply {
                artworkId?.let { putExtra(EXTRA_ARTWORK_ID, it) }
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                context.startActivity(this)
            }
        }
    }

    private val futures: MutableList<CompletableFuture<Void>> = ArrayList()
    private var arFragment: ArFragment? = null
    private var database: AugmentedImageDatabase? = null
    private var plainVideoModel: Renderable? = null
    private var plainVideoMaterial: Material? = null
    private var mediaPlayer: MediaPlayer? = null
    private var activeAugmentedImage: AugmentedImage? = null
    private var currentVideoNode: TransformableNode? = null
    private var currentArtworkId: String? = null
    private lateinit var videoCache: ARVideoCache
    private val targetArtworkId: String? by lazy { intent.getStringExtra(EXTRA_ARTWORK_ID) }

    private val viewModel: ArtworkARViewModel by viewModel<ArtworkARViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_artwork_ar)

        videoCache = ARVideoCache(this)
        addCloseButton()
        observeArtworks()

        supportFragmentManager.addFragmentOnAttachListener(this)

        if (savedInstanceState == null) {
            if (Sceneform.isSupported(this)) {
                supportFragmentManager.beginTransaction()
                    .add(R.id.ar_fragment, ArFragment::class.java, null)
                    .commit()
            }
        }

        if (Sceneform.isSupported(this)) {
            loadMatrixModel()
            loadMatrixMaterial()
        }
    }

    override fun onAttachFragment(fragmentManager: FragmentManager, fragment: Fragment) {
        if (fragment.id == R.id.ar_fragment) {
            arFragment = fragment as ArFragment
            arFragment!!.setOnSessionConfigurationListener(this)
        }
    }

    override fun onSessionConfiguration(session: Session?, config: Config) {
        config.setFocusMode(Config.FocusMode.AUTO)
        config.planeFindingMode = Config.PlaneFindingMode.DISABLED

        database = AugmentedImageDatabase(session)
        config.setAugmentedImageDatabase(database)

        arFragment!!.setOnAugmentedImageUpdateListener { augmentedImage: AugmentedImage ->
            this.onAugmentedImageTrackingUpdate(
                augmentedImage
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        futures.forEach(Consumer { future: CompletableFuture<Void> ->
            if (!future.isDone) future.cancel(true)
        })

        mediaPlayer?.let { player ->
            try {
                player.stop()
                player.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping media player: ${e.message}")
            }
        }

        videoCache.cleanup()
    }

    private fun loadMatrixModel() {
        futures.add(
            ModelRenderable.builder()
                .setSource(this, "models/Video.glb".toUri())
                .setIsFilamentGltf(true)
                .build()
                .thenAccept { model ->
                    model.setShadowCaster(false)
                    model.setShadowReceiver(true)
                    plainVideoModel = model
                }
                .exceptionally {
                    null
                })
    }

    private fun loadMatrixMaterial() {
        val filamentEngine = EngineInstance.getEngine().filamentEngine

        MaterialBuilder.init()
        val materialBuilder = MaterialBuilder()
            .flipUV(false)
            .platform(MaterialBuilder.Platform.MOBILE)
            .name("External Video Material")
            .require(MaterialBuilder.VertexAttribute.UV0)
            .shading(MaterialBuilder.Shading.UNLIT)
            .samplerParameter(
                MaterialBuilder.SamplerType.SAMPLER_EXTERNAL,
                MaterialBuilder.SamplerFormat.FLOAT,
                MaterialBuilder.ParameterPrecision.DEFAULT,
                "videoTexture"
            )

        val plainVideoMaterialPackage = materialBuilder
            .blending(MaterialBuilder.BlendingMode.OPAQUE)
            .material(
                """
                void material(inout MaterialInputs material) {
                  prepareMaterial(material);
                  material.baseColor = texture(materialParams_videoTexture, getUV0()).rgba;
                  material.baseColor.rgb = inverseTonemapSRGB(material.baseColor.rgb);
                }
                """.trimIndent()
            )
            .build(filamentEngine)
        if (plainVideoMaterialPackage.isValid) {
            val buffer: ByteBuffer = plainVideoMaterialPackage.buffer
            futures.add(
                Material.builder()
                    .setSource(buffer)
                    .build()
                    .thenAccept { material ->
                        plainVideoMaterial = material
                    }
                    .exceptionally { throwable ->
                        Toast.makeText(this, "Unable to load material", Toast.LENGTH_LONG)
                            .show()
                        null
                    })
        }
        MaterialBuilder.shutdown()
    }

    private fun onAugmentedImageTrackingUpdate(augmentedImage: AugmentedImage) {
        val id = augmentedImage.name

        if (currentArtworkId == id) {
            return
        }

        if (augmentedImage.trackingState == TrackingState.TRACKING &&
            augmentedImage.trackingMethod == AugmentedImage.TrackingMethod.FULL_TRACKING
        ) {
            playARVideo(augmentedImage)
        }
    }


    private fun observeArtworks() {
        lifecycleScope.launch {
            viewModel.artworks.collect { async ->
                if (async is Async.Success) {
                    val artworks = async()
                    preloadTargetArtwork(artworks)
                    loadArtworksIntoDatabase(artworks)
                }
            }
        }
    }

    private fun preloadTargetArtwork(artworks: List<Artwork>) {
        lifecycleScope.launch(Dispatchers.IO) {
            val artwork = artworks.find { it.id == targetArtworkId } ?: return@launch
            val videoURL = artwork.arDigitalAssetURL

            if (videoURL != null) {
                downloadImage(artwork)
                videoCache.preload(artwork.id, videoURL)
            }

            withContext(Dispatchers.Main) {
                configureARSession()
            }
        }
    }

    private fun loadArtworksIntoDatabase(artworks: List<Artwork>) {
        lifecycleScope.launch(Dispatchers.IO) {
            artworks.forEach { artwork ->
                downloadImage(artwork)
            }

            withContext(Dispatchers.Main) {
                configureARSession()
            }
        }
    }

    private suspend fun downloadImage(artwork: Artwork) {
        val imageURL = artwork.mediaLarge ?: return

        try {
            val request = ImageRequest.Builder(this@ArtworkARActivity)
                .data(imageURL.toString())
                .allowHardware(false)
                .build()
            val result = applicationContext.imageLoader.execute(request)
            val drawable = result.drawable
            val bitmap = (drawable as? BitmapDrawable)?.bitmap

            if (bitmap != null) {
                database?.addImage(artwork.id, bitmap)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading image for ${artwork.id}: ${e.message}", e)
        }
    }

    private fun cleanupCurrentVideo() {
        mediaPlayer?.let { player ->
            try {
                if (player.isPlaying) {
                    player.pause()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping current video: ${e.message}", e)
            }
        }
        mediaPlayer = null

        // Remove video node from scene
        currentVideoNode?.let { node ->
            node.parent?.removeChild(node)
            currentVideoNode = null
        }

        currentArtworkId = null
    }

    private fun playARVideo(augmentedImage: AugmentedImage) {
        val id = augmentedImage.name
        val videoUrl = viewModel.artworkVideos[id]

        if (videoUrl == null) {
            return
        }

        cleanupCurrentVideo()

        activeAugmentedImage = augmentedImage
        currentArtworkId = id

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val cachedVideo = videoCache.get(id, videoUrl)

                if (cachedVideo?.mediaPlayer != null) {
                    withContext(Dispatchers.Main) {
                        if (currentArtworkId == id) {
                            mediaPlayer = cachedVideo.mediaPlayer
                            mediaPlayer?.start()
                            setupVideoAnchor(augmentedImage)
                        }
                    }
                } else {
                    currentArtworkId = null
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error playing cached video for artwork $id: ${e.message}", e)
                currentArtworkId = null
            }
        }
    }

    private fun setupVideoAnchor(augmentedImage: AugmentedImage) {
        val anchorNode = AnchorNode(augmentedImage.createAnchor(augmentedImage.centerPose))
        anchorNode.setWorldScale(
            Vector3(
                augmentedImage.extentX,
                1f,
                augmentedImage.extentZ
            )
        )
        arFragment!!.arSceneView.scene.addChild(anchorNode)

        val videoNode = TransformableNode(arFragment!!.transformationSystem)
        anchorNode.addChild(videoNode)

        // Track the current video node for cleanup
        currentVideoNode = videoNode

        val externalTexture = ExternalTexture()
        val renderableInstance = videoNode.setRenderable(plainVideoModel)
        renderableInstance.material = plainVideoMaterial

        renderableInstance.material.setExternalTexture("videoTexture", externalTexture)
        mediaPlayer?.setSurface(externalTexture.surface)
    }

    private fun configureARSession() {
        arFragment?.arSceneView?.session?.let { session ->
            try {
                val config = session.config
                config.setAugmentedImageDatabase(database)
                session.configure(config)
                session.resume()
            } catch (e: Exception) {
                Log.e(TAG, "Error reconfiguring AR session: ${e.message}", e)
            }
        }
    }

    private fun addCloseButton() {
        findViewById<ComposeView>(R.id.close_button).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ArtGalleryTheme {
                    CloseButton {
                        finish()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CloseButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.statusBarsIgnoringVisibility)
            .padding(top = 8.dp, start = 8.dp)
    ) {
        CloseIconButton(onClick = onClick)
    }
}