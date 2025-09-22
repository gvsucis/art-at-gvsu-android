package edu.gvsu.art.gallery.ui.artwork.detail

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.Matrix
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.FragmentActivity
import com.google.ar.core.Anchor
import com.google.ar.core.AugmentedImage
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.UnavailableApkTooOldException
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException
import com.google.ar.core.exceptions.UnavailableSdkTooOldException
import edu.gvsu.art.gallery.R
import edu.gvsu.art.gallery.ui.CloseIconButton
import edu.gvsu.art.gallery.ui.theme.ArtGalleryTheme
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class ArtworkARActivity : FragmentActivity() {
    private var artworkDetected = false
    private var database: AugmentedImageDatabase? = null
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var videoPath: Uri
    private lateinit var imagePath: Uri
    private var arSession: Session? = null
    private var surfaceTexture: SurfaceTexture? = null
    private var videoSurface: Surface? = null
    private var videoAnchor: Anchor? = null
    private var videoTextureId = 0

    // OpenGL resources for video rendering
    private var shaderProgram = 0
    private var vertexBuffer: FloatBuffer? = null
    private var textureCoordBuffer: FloatBuffer? = null

    // Vertex shader for video plane
    private val vertexShaderCode = """
        attribute vec4 vPosition;
        attribute vec2 aTexCoord;
        uniform mat4 uMVPMatrix;
        varying vec2 vTexCoord;
        void main() {
            gl_Position = uMVPMatrix * vPosition;
            vTexCoord = aTexCoord;
        }
    """.trimIndent()

    // Fragment shader for external texture
    private val fragmentShaderCode = """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;
        varying vec2 vTexCoord;
        uniform samplerExternalOES sTexture;
        void main() {
            gl_FragColor = texture2D(sTexture, vTexCoord);
        }
    """.trimIndent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_artwork_ar)

        addCloseButton()

        videoPath = Uri.parse(intent.getStringExtra(EXTRA_AR_VIDEO_PATH))
        imagePath = Uri.parse(intent.getStringExtra(EXTRA_AR_IMAGE_PATH))

        setupARCore()
    }

    private fun setupARCore() {
        try {
            // Create AR session
            arSession = Session(this)

            // Configure session for augmented images
            val config = Config(arSession).apply {
                focusMode = Config.FocusMode.AUTO
                planeFindingMode = Config.PlaneFindingMode.DISABLED
            }

            // Setup augmented image database
            setupAugmentedImageDatabase(config)

            arSession?.configure(config)

            // Initialize OpenGL resources
            initializeOpenGL()

            Log.d("ArtworkAR", "ARCore setup completed successfully")

        } catch (e: UnavailableArcoreNotInstalledException) {
            Toast.makeText(this, "ARCore not installed", Toast.LENGTH_LONG).show()
            finish()
        } catch (e: UnavailableApkTooOldException) {
            Toast.makeText(this, "ARCore too old, please update", Toast.LENGTH_LONG).show()
            finish()
        } catch (e: UnavailableSdkTooOldException) {
            Toast.makeText(this, "SDK too old for ARCore", Toast.LENGTH_LONG).show()
            finish()
        } catch (e: Exception) {
            Log.e("ArtworkAR", "Error setting up ARCore", e)
            Toast.makeText(this, "Error setting up AR: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupAugmentedImageDatabase(config: Config) {
        try {
            arSession?.let { session ->
                database = AugmentedImageDatabase(session)

                val matrixImage = BitmapFactory.decodeStream(contentResolver.openInputStream(imagePath))
                database?.addImage(IMAGE_KEY, matrixImage)
                database?.let { db ->
                    config.augmentedImageDatabase = db
                }

                Log.d("ArtworkAR", "Augmented image database configured")
            }
        } catch (e: Exception) {
            Log.e("ArtworkAR", "Error setting up image database", e)
            Toast.makeText(this, "Unable to load target image", Toast.LENGTH_LONG).show()
        }
    }

    private fun initializeOpenGL() {
        // Create external texture for video
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        videoTextureId = textures[0]

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, videoTextureId)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

        // Create SurfaceTexture for video playback
        surfaceTexture = SurfaceTexture(videoTextureId)
        videoSurface = Surface(surfaceTexture)

        // Initialize vertex buffers for video plane
        initializeVertexBuffers()

        // Create shader program
        shaderProgram = createShaderProgram()

        Log.d("ArtworkAR", "OpenGL initialized successfully")
    }

    private fun initializeVertexBuffers() {
        // Define vertices for a quad (plane)
        val vertices = floatArrayOf(
            -0.5f, -0.5f, 0.0f,  // bottom left
             0.5f, -0.5f, 0.0f,  // bottom right
            -0.5f,  0.5f, 0.0f,  // top left
             0.5f,  0.5f, 0.0f   // top right
        )

        val textureCoords = floatArrayOf(
            0.0f, 1.0f,  // bottom left
            1.0f, 1.0f,  // bottom right
            0.0f, 0.0f,  // top left
            1.0f, 0.0f   // top right
        )

        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(vertices)
                position(0)
            }
        }

        textureCoordBuffer = ByteBuffer.allocateDirect(textureCoords.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(textureCoords)
                position(0)
            }
        }
    }

    private fun createShaderProgram(): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        return GLES20.glCreateProgram().also { program ->
            GLES20.glAttachShader(program, vertexShader)
            GLES20.glAttachShader(program, fragmentShader)
            GLES20.glLinkProgram(program)
        }
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }

    private fun handleAugmentedImages(frame: Frame) {
        val updatedAugmentedImages = frame.getUpdatedTrackables(AugmentedImage::class.java)

        for (augmentedImage in updatedAugmentedImages) {
            when (augmentedImage.trackingState) {
                TrackingState.TRACKING -> {
                    if (!artworkDetected && augmentedImage.name == IMAGE_KEY) {
                        artworkDetected = true
                        createVideoNode(augmentedImage)
                    }
                }
                TrackingState.PAUSED -> {
                    mediaPlayer?.pause()
                }
                TrackingState.STOPPED -> {
                    artworkDetected = false
                    removeVideoNode()
                }
            }
        }
    }

    private fun createVideoNode(augmentedImage: AugmentedImage) {
        try {
            // Create anchor at the center of the detected image
            videoAnchor = augmentedImage.createAnchor(augmentedImage.centerPose)

            // Setup MediaPlayer with the external texture surface
            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@ArtworkARActivity, videoPath)
                setSurface(videoSurface)
                prepareAsync()
                isLooping = true
                setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)

                setOnPreparedListener { player ->
                    player.start()
                    Log.d("ArtworkAR", "Video playback started")
                }

                setOnErrorListener { _, what, extra ->
                    Log.e("ArtworkAR", "Video playback error: what=$what, extra=$extra")
                    Toast.makeText(this@ArtworkARActivity, "Video playback failed", Toast.LENGTH_SHORT).show()
                    true
                }
            }

            Log.d("ArtworkAR", "Video node created successfully")

        } catch (e: Exception) {
            Log.e("ArtworkAR", "Error creating video node", e)
            Toast.makeText(this, "Error creating AR video: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun removeVideoNode() {
        try {
            videoAnchor?.detach()
            videoAnchor = null

            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                reset()
            }

            Log.d("ArtworkAR", "Video node removed successfully")
        } catch (e: Exception) {
            Log.e("ArtworkAR", "Error removing video node", e)
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            arSession?.resume()
        } catch (e: CameraNotAvailableException) {
            Log.e("ArtworkAR", "Camera not available", e)
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        arSession?.pause()
        mediaPlayer?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()

        // Clean up video resources
        removeVideoNode()

        mediaPlayer?.apply {
            stop()
            release()
        }
        mediaPlayer = null

        // Clean up OpenGL resources
        videoSurface?.release()
        surfaceTexture?.release()

        if (videoTextureId != 0) {
            GLES20.glDeleteTextures(1, intArrayOf(videoTextureId), 0)
        }

        if (shaderProgram != 0) {
            GLES20.glDeleteProgram(shaderProgram)
        }

        // Clean up AR session
        arSession?.close()
        arSession = null
    }

    private fun addCloseButton() {
        findViewById<ComposeView>(R.id.close_button).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ArtGalleryTheme {
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