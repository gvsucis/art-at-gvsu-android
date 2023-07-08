package edu.gvsu.art.gallery.extensions

import android.content.Context
import android.content.Intent
import edu.gvsu.art.client.Artwork
import edu.gvsu.art.gallery.lib.Links

fun Context.shareArtwork(artwork: Artwork) {
    val share = Intent.createChooser(Intent().apply {
        type = "text/plain"
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, Links.artworkDetail(artwork.id))
        putExtra(Intent.EXTRA_TITLE, "${artwork.name} - ${artwork.formattedArtistName}")
    }, null)
    startActivity(share)
}
