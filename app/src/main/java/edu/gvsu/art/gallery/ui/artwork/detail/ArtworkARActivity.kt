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
import coil.ImageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
import edu.gvsu.art.gallery.lib.Async
import edu.gvsu.art.gallery.ui.CloseIconButton
import edu.gvsu.art.gallery.ui.theme.ArtGalleryTheme
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.nio.ByteBuffer
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer


class ArtworkARActivity : FragmentActivity(), FragmentOnAttachListener,
    BaseArFragment.OnSessionConfigurationListener {
    private val futures: MutableList<CompletableFuture<Void>> = ArrayList()
    private var arFragment: ArFragment? = null
    private var artworkDetected = false
    private var database: AugmentedImageDatabase? = null
    private var plainVideoModel: Renderable? = null
    private var plainVideoMaterial: Material? = null
    private var mediaPlayer: MediaPlayer? = null

    private val viewModel: ArtworkARViewModel by viewModel<ArtworkARViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_artwork_ar)

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
        session?.apply {
            pause()
            resume()
            pause()
        }

        database = AugmentedImageDatabase(session)
        Log.d("gv.ar", "Created AugmentedImageDatabase with ${database?.numImages ?: 0} images")
        config.setAugmentedImageDatabase(database)
        Log.d("gv.ar", "Set database on AR config")

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

        if (mediaPlayer != null) {
            mediaPlayer!!.stop()
            mediaPlayer!!.reset()
        }
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
        Log.d("gv.ar", "id=${augmentedImage.name} artworkDetected=$artworkDetected")
        if (artworkDetected) {
            return
        }

        if ((augmentedImage.trackingState === TrackingState.TRACKING && augmentedImage.getTrackingMethod() === AugmentedImage.TrackingMethod.FULL_TRACKING)) {
            val anchorNode = AnchorNode(augmentedImage.createAnchor(augmentedImage.getCenterPose()))
            val id = augmentedImage.name
            val url = viewModel.artworkVideos[id]

            if (!artworkDetected &&  url != null) {
                Log.d("gv.ar", "id=$augmentedImage; url=$url")
                artworkDetected = true
                anchorNode.setWorldScale(
                    Vector3(
                        augmentedImage.getExtentX(),
                        1f,
                        augmentedImage.getExtentZ()
                    )
                )
                arFragment!!.arSceneView.scene.addChild(anchorNode)

                val videoNode = TransformableNode(arFragment!!.transformationSystem)
                anchorNode.addChild(videoNode)

                val externalTexture = ExternalTexture()
                val renderableInstance = videoNode.setRenderable(plainVideoModel)
                renderableInstance.material = plainVideoMaterial

                renderableInstance.material.setExternalTexture("videoTexture", externalTexture)
//                mediaPlayer = MediaPlayer.create(this, videoPath).apply {
//                    isLooping = true
//                    setSurface(externalTexture.surface)
//                    start()
//                }
            }
        }
    }

    private fun observeArtworks() {
        lifecycleScope.launch {
            viewModel.artworks.collect { async ->
                if (async is Async.Success) {
                    loadArtworksIntoDatabase(async())
                }
            }
        }
    }

    private fun loadArtworksIntoDatabase(artworks: List<Artwork>) {
        val imageLoader = ImageLoader(this)
        
        lifecycleScope.launch {
            artworks.forEach { artwork ->
                artwork.mediaLarge?.let { imageUrl ->
                    try {
                        Log.d("gv.ar", "Loading image for ${artwork.id}: $imageUrl")
                        val request = ImageRequest.Builder(this@ArtworkARActivity)
                            .data(imageUrl.toString())
                            .allowHardware(false)
                            .build()
                        
                        val result = imageLoader.execute(request)
                        val drawable = result.drawable
                        val bitmap = (drawable as? BitmapDrawable)?.bitmap
                        
                        if (bitmap != null) {
                            val index = database?.addImage(artwork.id, bitmap)
                            Log.d("gv.ar", "Added image ${artwork.id} to database at index: $index")
                        } else {
                            Log.w("gv.ar", "Failed to get bitmap for ${artwork.id}")
                        }
                    } catch (e: Exception) {
                        Log.e("gv.ar", "Error loading image for ${artwork.id}: ${e.message}", e)
                    }
                }
            }

            Log.d("gv.ar", "Database now has ${database?.numImages} images")
            reconfigureARSession()
        }
    }

    private fun reconfigureARSession() {
        arFragment?.arSceneView?.session?.let { session ->
            Log.d("gv.ar", "Reconfiguring AR session with ${database?.numImages} images")
            val config = Config(session)
            config.setFocusMode(Config.FocusMode.AUTO)
            config.planeFindingMode = Config.PlaneFindingMode.DISABLED
            config.setAugmentedImageDatabase(database)
            session.configure(config)
            Log.d("gv.ar", "AR session reconfigured")
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

    companion object {
        const val IMAGE_KEY = "ar_image"

        const val EXTRA_AR_VIDEO_PATH = "EXTRA_AR_VIDEO_PATH"
        const val EXTRA_AR_IMAGE_PATH = "EXTRA_AR_IMAGE_PATH"

        fun start(context: Context, arAssets: ArtworkARAssets) {
            Intent(context, ArtworkARActivity::class.java).apply {
                putExtra(EXTRA_AR_VIDEO_PATH, arAssets.video.toString())
                putExtra(EXTRA_AR_IMAGE_PATH, arAssets.image.toString())
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                context.startActivity(this)
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