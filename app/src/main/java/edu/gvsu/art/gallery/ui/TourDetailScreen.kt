package edu.gvsu.art.gallery.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.ktx.addMarker
import com.google.maps.android.ktx.awaitMap
import edu.gvsu.art.client.Tour
import edu.gvsu.art.client.TourStop
import edu.gvsu.art.gallery.R
import edu.gvsu.art.gallery.lib.Async
import edu.gvsu.art.gallery.navigateToArtworkDetail
import edu.gvsu.art.gallery.ui.foundation.LocalTabScreen
import edu.gvsu.art.gallery.ui.theme.LightBlue
import edu.gvsu.art.gallery.ui.theme.Shapes
import edu.gvsu.art.gallery.ui.theme.isAppInDarkTheme
import kotlinx.coroutines.launch


@Composable
fun TourDetailScreen(navController: NavController, tourID: String?, tourName: String) {
    tourID ?: return
    val tabScreen = LocalTabScreen.current
    val (data, refresh) = useTour(tourID)
    val coroutineScope = rememberCoroutineScope()

    fun navigateToArtwork(artworkID: String) {
        coroutineScope.launch {
            navController.navigateToArtworkDetail(tabScreen, artworkID)
        }
    }

    Scaffold(
        topBar = {
            GalleryTopAppBar(
                title = tourName,
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (data) {
                is Async.Success ->
                    TourDetailView(
                        data(),
                        navigateToArtwork = { navigateToArtwork(it) }
                    )
                is Async.Failure ->
                    ErrorView(
                        error = data.error,
                        onRetryClick = { refresh() }
                    )

                else -> LoadingView()
            }
        }
    }
}

@Composable
private fun TourDetailView(tour: Tour, navigateToArtwork: (artworkID: String) -> Unit) {
    val tourStops = tour.stops
    val listState = rememberLazyListState()
    val (selectedTourStopIndex, setSelectedTourStopIndex) = rememberSaveable { mutableStateOf<Int>(-1) }
    val selectedTourStop = tourStops.getOrNull(selectedTourStopIndex)

    val coroutineScope = rememberCoroutineScope()

    fun scrollToArtwork(artworkID: String) {
        coroutineScope.launch {
            val currentIndex = tourStops.indexOfFirst { it.artworkID == artworkID }
            setSelectedTourStopIndex(currentIndex)

            if (currentIndex > -1) {
                listState.animateScrollToItem(currentIndex)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(0.8f)) {
            MapView(
                tour = tour,
                selectedTourStop = selectedTourStop,
                onMarkerClick = { artworkID -> scrollToArtwork(artworkID) },
                onMarkerInfoWindowClick = { artworkID -> navigateToArtwork(artworkID) }
            )
        }
        LazyRow(
            state = listState,
            modifier = Modifier
                .height(144.dp)
                .background(MaterialTheme.colors.surface)
        ) {
            itemsIndexed(tourStops, key = { _, stop -> stop.artworkID }) { index, tourStop ->
                TourStopCell(
                    tourStop,
                    isSelected = index == selectedTourStopIndex,
                    onClick = { setSelectedTourStopIndex(index) }
                )
            }
        }
    }
}

@SuppressLint("PotentialBehaviorOverride")
@Composable
private fun MapView(
    tour: Tour,
    selectedTourStop: TourStop?,
    onMarkerClick: (artworkID: String) -> Unit,
    onMarkerInfoWindowClick: (artworkID: String) -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val isDarkTheme = isAppInDarkTheme()
    val boundingPadding = with(LocalDensity.current) { 32.dp.roundToPx() }
    val map = rememberMapViewWithLifecycle()
    val (googleMap, setGoogleMap) = remember { mutableStateOf<GoogleMap?>(null) }
    val (tourStopMarkers, setTourStopMarkers) = remember { mutableStateOf<List<Marker>>(emptyList()) }
    val markerSnippet = stringResource(R.string.tour_marker_more_details)

    AndroidView(
        modifier = Modifier.wrapContentHeight(unbounded = false),
        factory = { map },
    ) { mapView ->
        coroutineScope.launch {
            if (googleMap == null) {
                val awaitedMap = mapView.awaitMap()
                if (isDarkTheme) {
                    awaitedMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                            context,
                            R.raw.google_maps_dark
                        )
                    )
                } else {
                    awaitedMap.setMapStyle(null)
                }
                val boundsBuilder = LatLngBounds.builder()

                val markers = mutableListOf<Marker>();
                tour.stops.forEach { tourStop ->
                    tourStop.location?.asGoogleMapsLatLng()?.let { googleLocation ->
                        boundsBuilder.include(googleLocation)
                        awaitedMap.addMarker {
                            title(tourStop.name)
                            snippet(markerSnippet)
                            position(googleLocation)
                        }?.also {
                            it.tag = tourStop.artworkID
                            markers.add(it)
                        }
                    }
                }
                val bounds = boundsBuilder.build()
                awaitedMap.moveCamera(
                    CameraUpdateFactory.newLatLngBounds(bounds, boundingPadding)
                )

                awaitedMap.setOnMarkerClickListener { marker ->
                    val markerTag = marker.tag
                    if (markerTag != null && markerTag is String) {
                        onMarkerClick(markerTag)
                    }
                    return@setOnMarkerClickListener false
                }
                awaitedMap.setOnInfoWindowClickListener { marker ->
                    val markerTag = marker.tag
                    if (markerTag is String) {
                        onMarkerInfoWindowClick(markerTag)
                    }
                }

                setTourStopMarkers(markers)
                setGoogleMap(awaitedMap)
            }
        }
    }

    LaunchedEffect(selectedTourStop) {
        selectedTourStop?.let { stop ->
            val marker = tourStopMarkers.find { it.tag == stop.artworkID }
            stop.location?.let {
                googleMap?.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        it.asGoogleMapsLatLng(),
                        18f
                    )
                )
                marker?.showInfoWindow()
            }
        }
    }
}

@Composable
fun TourStopCell(
    tourStop: TourStop,
    onClick: (tourStop: TourStop) -> Unit = {},
    isSelected: Boolean = false,
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp, vertical = 8.dp)
            .selectedBorder(isSelected)
            .clip(Shapes.large)
            .clickable { onClick(tourStop) }
    ) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colors.onSurface)
                .aspectRatio(1f)
        )
        AsyncImage(
            model = tourStop.media.toString(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .aspectRatio(1f)
        )
    }
}

private fun Modifier.selectedBorder(isSelected: Boolean) =
    if (isSelected) {
        this.then(Modifier.border(BorderStroke(4.dp, LightBlue), Shapes.large))
    } else {
        this
    }
