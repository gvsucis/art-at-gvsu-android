package edu.gvsu.art.gallery.ui.artwork.ar

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.net.toUri
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

    private lateinit var videoPath: Uri
    private lateinit var imagePath: Uri

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        videoPath = arguments?.getParcelable(ARG_VIDEO_PATH) ?: error("Missing video path")
        imagePath = arguments?.getParcelable(ARG_IMAGE_PATH) ?: error("Missing image path")

        sceneView = view.findViewById<ARSceneView>(R.id.sceneView).apply {
            configureSession { session, config ->
                config.setFocusMode(Config.FocusMode.AUTO)
                config.planeFindingMode = Config.PlaneFindingMode.DISABLED

                config.addAugmentedImage(
                    session,
                    IMAGE_KEY,
                    requireContext().contentResolver.openInputStream(imagePath)
                        ?.use(BitmapFactory::decodeStream)
                        ?: error("Failed to load image from $imagePath")
                )
            }
            onSessionUpdated = { session, frame ->
                frame.getUpdatedAugmentedImages().forEach { augmentedImage ->
                    if (augmentedImageNodes.none { it.imageName == augmentedImage.name }) {
                        val augmentedImageNode = AugmentedImageNode(
                            engine = engine,
                            augmentedImage = augmentedImage,
                        ).apply {
                            when (augmentedImage.name) {
                                IMAGE_KEY -> {
                                    var hasResized = false

                                    onUpdated = { image ->
                                        if (!hasResized && image.extentX > 0 && image.extentZ > 0) {
                                            val player =
                                                exoPlayer ?: ExoPlayer.Builder(requireContext())
                                                    .build().also {
                                                        it.setMediaItem(MediaItem.fromUri(videoPath))
                                                        it.prepare()
                                                        it.playWhenReady = true
                                                        it.repeatMode = Player.REPEAT_MODE_ALL
                                                        exoPlayer = it
                                                    }

                                            val videoNode = ExoPlayerNode(
                                                engine = engine,
                                                size = Size(
                                                    x = image.extentX,
                                                    y = 0.0f,
                                                    z = image.extentZ
                                                ),
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
        exoPlayer?.release()
        exoPlayer = null
    }

    companion object {
        const val ARG_VIDEO_PATH = "ARG_VIDEO_PATH"
        const val ARG_IMAGE_PATH = "ARG_IMAGE_PATH"
        const val IMAGE_KEY = "ar_image"
    }
}
