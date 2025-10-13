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
import io.github.sceneview.math.Position

class ArtworkARContentFragment : Fragment(R.layout.fragment_artwork_ar_content) {

    lateinit var sceneView: ARSceneView

    val augmentedImageNodes = mutableListOf<AugmentedImageNode>()

    private var exoPlayer: ExoPlayer? = null

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
                        ).apply {
                            when (name) {
                                "qrcode" -> {
                                    var hasResized = false

                                    onUpdated = { image ->
                                        if (!hasResized && image.extentX > 0 && image.extentZ > 0) {
                                            val player = exoPlayer ?: ExoPlayer.Builder(requireContext()).build().also {
                                                it.setMediaItem(MediaItem.fromUri("https://artgallery.gvsu.edu/admin/media/collectiveaccess/quicktime/1/1/3/5/6/10629_ca_attribute_values_value_blob_1135655_original.mp4"))
                                                it.prepare()
                                                it.playWhenReady = true
                                                it.repeatMode = Player.REPEAT_MODE_ALL
                                                exoPlayer = it
                                            }

                                            val videoNode = ExoPlayerNode(
                                                engine = engine,
                                                size = Size(x = image.extentX, y = 0.0f, z = image.extentZ),
                                                materialLoader = materialLoader,
                                                exoPlayer = player,
                                            )

                                            hasResized = true
                                            addChildNode(videoNode)
                                        }
                                    }
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

    override fun onDestroyView() {
        super.onDestroyView()
        augmentedImageNodes.forEach { node ->
            node.destroy()
        }
        augmentedImageNodes.clear()
        exoPlayer?.release()
        exoPlayer = null
    }
}
