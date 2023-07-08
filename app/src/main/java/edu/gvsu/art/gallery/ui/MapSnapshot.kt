package edu.gvsu.art.gallery.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.ktx.addMarker
import com.google.maps.android.ktx.awaitMap
import edu.gvsu.art.client.LatLng
import edu.gvsu.art.gallery.R
import edu.gvsu.art.gallery.ui.theme.isAppInDarkTheme
import kotlinx.coroutines.launch

@Composable
fun MapSnapshot(
    modifier: Modifier = Modifier,
    location: LatLng,
    zoom: Float = InitialZoom,
) {
    Box(
        modifier = modifier
    ) {
        val mapView = rememberMapViewWithLifecycle()
        CachedMapSnapshot(
            map = mapView,
            location = location,
            zoom = zoom
        )
    }
}

/**
 * [Original source](https://www.droidcon.com/2021/10/14/display-map-snapshot-using-jetpack-compose/)
 */
@Composable
private fun CachedMapSnapshot(map: MapView, location: LatLng, zoom: Float) {
    val googleLocation = location.asGoogleMapsLatLng()
    val mapBitmap = remember { mutableStateOf<Bitmap?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val darkTheme = isAppInDarkTheme()

    if (mapBitmap.value != null) {
        Image(
            bitmap = mapBitmap.value!!.asImageBitmap(),
            contentDescription = null
        )
    } else {
        AndroidView({ map }) { mapView ->
            coroutineScope.launch {
                val googleMap = mapView.awaitMap()
                if (darkTheme) {
                    googleMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                            context,
                            R.raw.google_maps_dark
                        )
                    )
                }

                googleMap.addMarker { position(googleLocation) }
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(googleLocation, zoom))

                // Wait until map is loaded to avoid cropping the bitmap
                googleMap.setOnMapLoadedCallback {
                    googleMap.snapshot {
                        mapBitmap.value = it
                    }
                }
            }
        }
    }
}

private const val InitialZoom = 14f
