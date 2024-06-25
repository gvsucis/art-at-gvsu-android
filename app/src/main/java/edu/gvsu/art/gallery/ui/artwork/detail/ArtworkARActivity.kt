package edu.gvsu.art.gallery.ui.artwork.detail

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentOnAttachListener
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
import edu.gvsu.art.gallery.R
import edu.gvsu.art.gallery.ui.CloseIconButton
import edu.gvsu.art.gallery.ui.theme.ArtAtGVSUTheme
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
    private lateinit var videoPath: Uri
    private lateinit var imagePath: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_artwork_ar)

        addCloseButton()

        videoPath = Uri.parse(intent.getStringExtra(EXTRA_AR_VIDEO_PATH))
        imagePath = Uri.parse(intent.getStringExtra(EXTRA_AR_IMAGE_PATH))

        supportFragmentManager.addFragmentOnAttachListener(this)

        if (savedInstanceState == null) {
            if (Sceneform.isSupported(this)) {
                supportFragmentManager.beginTransaction()
                    .add(R.id.arFragment, ArFragment::class.java, null)
                    .commit()
            }
        }

        if (Sceneform.isSupported(this)) {
            loadMatrixModel()
            loadMatrixMaterial()
        }
    }

    override fun onAttachFragment(fragmentManager: FragmentManager, fragment: Fragment) {
        if (fragment.id == R.id.arFragment) {
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

        val matrixImage = BitmapFactory.decodeStream(contentResolver.openInputStream(imagePath))

        database!!.addImage(IMAGE_KEY, matrixImage)

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

        if (mediaPlayer != null) {
            mediaPlayer!!.stop()
            mediaPlayer!!.reset()
        }
    }

    private fun loadMatrixModel() {
        futures.add(ModelRenderable.builder()
            .setSource(this, Uri.parse("models/Video.glb"))
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
            futures.add(Material.builder()
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
        if (artworkDetected) {
            return
        }

        if ((augmentedImage.trackingState === TrackingState.TRACKING && augmentedImage.getTrackingMethod() === AugmentedImage.TrackingMethod.FULL_TRACKING)) {
            val anchorNode = AnchorNode(augmentedImage.createAnchor(augmentedImage.getCenterPose()))

            if (!artworkDetected && augmentedImage.getName().equals(IMAGE_KEY)) {
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
                mediaPlayer = MediaPlayer.create(this, videoPath).apply {
                    isLooping = true
                    setSurface(externalTexture.surface)
                    start()
                }
            }
        }
    }

    private fun addCloseButton() {
        findViewById<ComposeView>(R.id.close_button).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ArtAtGVSUTheme {
                    CloseIconButton {
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
