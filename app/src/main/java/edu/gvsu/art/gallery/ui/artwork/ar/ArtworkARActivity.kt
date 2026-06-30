package edu.gvsu.art.gallery.ui.artwork.ar

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import edu.gvsu.art.gallery.ui.theme.ArtGalleryTheme

/**
 * Hosts the full-screen roaming AR experience that tracks every featured AR artwork
 * at once (iOS parity). Launched from the artwork detail screen and the Featured AR
 * collection — neither passes per-artwork assets; the session loads the set itself.
 */
class ArtworkARActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            ArtGalleryTheme {
                ARExperienceScreen(onClose = { finish() })
            }
        }
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, ArtworkARActivity::class.java))
        }
    }
}
