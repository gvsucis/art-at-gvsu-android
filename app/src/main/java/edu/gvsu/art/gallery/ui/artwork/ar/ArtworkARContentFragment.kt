package edu.gvsu.art.gallery.ui.artwork.ar

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.google.ar.core.Config
import edu.gvsu.art.gallery.R
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.arcore.addAugmentedImage
import io.github.sceneview.ar.arcore.getUpdatedAugmentedImages
import io.github.sceneview.ar.node.AugmentedImageNode
import io.github.sceneview.math.Size

class ArtworkARContentFragment : Fragment(R.layout.fragment_artwork_ar_content) {

    lateinit var sceneView: ARSceneView

    val augmentedImageNodes = mutableListOf<AugmentedImageNode>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sceneView = view.findViewById<ARSceneView>(R.id.sceneView).apply {
            configureSession { session, config ->
                config.setFocusMode(Config.FocusMode.AUTO)
                config.planeFindingMode = Config.PlaneFindingMode.DISABLED

                config.addAugmentedImage(
                    session, "qrcode",
                    requireContext().assets.open("augmentedimages/qrcode.jpg")
                        .use(BitmapFactory::decodeStream)
                )
            }
            onSessionUpdated = { session, frame ->
                frame.getUpdatedAugmentedImages().forEach { augmentedImage ->
                    if (augmentedImageNodes.none { it.imageName == augmentedImage.name }) {
                        val augmentedImageNode = AugmentedImageNode(
                            engine = engine,
                            augmentedImage = augmentedImage,
                            applyImageScale = true
                        ).apply {
                            when (augmentedImage.name) {
                                "qrcode" -> {
                                    Log.d("fragment", "x=${augmentedImage.extentX} z=${augmentedImage.extentZ}")
                                    addChildNode(
                                        ExoPlayerNode(
                                            engine = engine,
                                            rotateToNode = true,
                                            materialLoader = materialLoader,
                                            exoPlayer = ExoPlayer.Builder(requireContext()).build()
                                                .apply {
                                                    setMediaItem(MediaItem.fromUri("https://artgallery.gvsu.edu/admin/media/collectiveaccess/quicktime/1/1/3/5/6/10629_ca_attribute_values_value_blob_1135655_original.mp4"))
                                                    prepare()
                                                    playWhenReady = true
                                                    repeatMode = Player.REPEAT_MODE_ALL
                                                },
                                        )
                                    )
                                }
                            }
                        }

                        addChildNode(augmentedImageNode)
                        augmentedImageNodes += augmentedImageNode
                    }
                }
            }
        }
    }
}
