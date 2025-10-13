package edu.gvsu.art.gallery.ui.artwork.ar

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
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

        supportFragmentManager.commit {
            add(R.id.containerFragment, ArtworkARContentFragment::class.java, Bundle())
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
        fun start(context: Context, arAssets: ArtworkARAssets) {
            Intent(context, ArtworkARActivity::class.java).apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                context.startActivity(this)
            }
        }
    }
}
