package edu.gvsu.art.gallery.ui.artwork.ar

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.net.toUri
import androidx.fragment.app.commit
import edu.gvsu.art.gallery.R
import edu.gvsu.art.gallery.ui.CloseIconButton
import edu.gvsu.art.gallery.ui.artwork.detail.ArtworkARAssets
import edu.gvsu.art.gallery.ui.foundation.setFullScreen
import edu.gvsu.art.gallery.ui.theme.ArtGalleryTheme

class ArtworkARActivity : AppCompatActivity(R.layout.activity_artwork_ar) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFullScreen(
            findViewById(R.id.rootView),
            fullScreen = true,
            hideSystemBars = false,
            fitsSystemWindows = false
        )

        addCloseButton()

        val videoPath = intent.getStringExtra(EXTRA_AR_VIDEO_PATH)?.toUri()
        val imagePath = intent.getStringExtra(EXTRA_AR_IMAGE_PATH)?.toUri()

        supportFragmentManager.commit {
            add(R.id.containerFragment, ArtworkARContentFragment::class.java, Bundle().apply {
                putParcelable(ArtworkARContentFragment.ARG_VIDEO_PATH, videoPath)
                putParcelable(ArtworkARContentFragment.ARG_IMAGE_PATH, imagePath)
            })
        }
    }


    private fun addCloseButton() {
        findViewById<ComposeView>(R.id.closeButton).apply {
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
        const val EXTRA_AR_VIDEO_PATH = "EXTRA_AR_VIDEO_PATH"
        const val EXTRA_AR_IMAGE_PATH = "EXTRA_AR_IMAGE_PATH"

        fun start(context: Context, arAssets: ArtworkARAssets) {
            Intent(context, ArtworkARActivity::class.java).apply {
                putExtra(EXTRA_AR_VIDEO_PATH, arAssets.videoURL.toString())
                putExtra(EXTRA_AR_IMAGE_PATH, arAssets.imageURL.toString())
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                context.startActivity(this)
            }
        }
    }
}
