package edu.gvsu.art.gallery.extensions

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.text.htmlEncode
import edu.gvsu.art.client.LatLng


/**
 * https://developers.google.com/maps/documentation/urls/android-intents#kotlin_2
 */
fun Context.openGoogleMaps(latLng: LatLng, pinName: String) {
    val encodedPinName = pinName.htmlEncode()
    val geoURI =
        Uri.parse("geo:${latLng.latitude},${latLng.longitude}?q=${latLng.latitude},${latLng.longitude}(${encodedPinName})")
    val mapIntent = Intent(Intent.ACTION_VIEW, geoURI).apply {
        setPackage("com.google.android.apps.maps")
    }
    mapIntent.resolveActivity(packageManager)?.let {
        startActivity(mapIntent)
    }
}
