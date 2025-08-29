package edu.gvsu.art.gallery.ui.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import edu.gvsu.art.gallery.Route
import edu.gvsu.art.gallery.navigateToArtistDetail
import edu.gvsu.art.gallery.navigateToArtworkDetail
import edu.gvsu.art.gallery.ui.foundation.LocalTopLevelRoute

@ExperimentalPermissionsApi
@ExperimentalComposeUiApi
@Composable
fun SearchIndexScreen(navController: NavController) {
    val tabScreen = LocalTopLevelRoute.current
    val (query, setQuery) = rememberSaveable { mutableStateOf("") }
    val (selectedModel, setModel) = rememberSaveable { mutableStateOf(SearchCategory.ARTWORK) }
    val (isQRDialogOpen, openQRDialog) = remember { mutableStateOf(false) }
    val (isVisionSearchOpen, openVisionSearch) = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier.statusBarsPadding()
            ) {
                SearchIndexSearchBar(
                    query = query,
                    selectedCategory = selectedModel,
                    setQuery = setQuery,
                    setCategory = setModel,
                    onSelectQRScanner = {
                        openQRDialog(true)
                    },
                    onSelectVisionSearch = {
                        openVisionSearch(true)
                    }
                )
            }
        }
    ) { padding ->
        Box(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            SearchIndexList(
                selected = selectedModel,
                query = query,
                onArtistSelect = { artist ->
                    navController.navigateToArtistDetail(tabScreen, artist.id)
                },
                onArtworkSelect = { artwork ->
                    navController.navigateToArtworkDetail(tabScreen, artwork.id)
                }
            )
        }
    }

    if (isQRDialogOpen) {
        QRScannerDialog(
            navController = navController,
            onDismiss = { openQRDialog(false) },
        )
    }

    if (isVisionSearchOpen) {
        VisionSearchDialog(
            onCapture = { navController.navigate(Route.VisionSearchResults(it.toString())) },
            onDismiss = { openVisionSearch(false) }
        )
    }
}
