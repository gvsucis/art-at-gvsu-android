package edu.gvsu.art.gallery.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings.*
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionRequired
import com.google.accompanist.permissions.rememberPermissionState
import edu.gvsu.art.gallery.BuildConfig
import edu.gvsu.art.gallery.R
import edu.gvsu.art.gallery.lib.Links
import edu.gvsu.art.gallery.navigateToArtistDetail
import edu.gvsu.art.gallery.navigateToArtworkDetail
import edu.gvsu.art.gallery.ui.foundation.LocalTabScreen

@ExperimentalPermissionsApi
@ExperimentalComposeUiApi
@Composable
fun SearchIndexScreen(navController: NavController) {
    val tabScreen = LocalTabScreen.current
    val (query, setQuery) = rememberSaveable { mutableStateOf("") }
    val (selectedModel, setModel) = rememberSaveable { mutableStateOf(SearchModel.ARTIST) }
    val (isQRDialogOpen, openQRDialog) = remember { mutableStateOf(false) }

    Column {
        SearchIndexSearchBar(
            query = query,
            selectedModel = selectedModel,
            setQuery = setQuery,
            setModel = setModel,
            selectQRScanner = {
                openQRDialog(true)
            }
        )
        SearchIndexModelList(
            selectedModel = selectedModel,
            query = query,
            onArtistSelect = { artist ->
                navController.navigateToArtistDetail(artist.id)
            },
            onArtworkSelect = { artwork ->
                navController.navigateToArtworkDetail(artwork.id)
            }
        )
    }

    if (isQRDialogOpen) {
        QRScannerDialog(
            navController = navController,
            onDismiss = { openQRDialog(false) },
        )
    }
}

@ExperimentalPermissionsApi
@ExperimentalComposeUiApi
@Composable
fun QRScannerDialog(
    onDismiss: () -> Unit,
    navController: NavController,
) {
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    val tabScreen = LocalTabScreen.current
    val (url, setURL) = remember { mutableStateOf("") }

    PermissionRequired(
        permissionState = cameraPermissionState,
        permissionNotGrantedContent = {
            LaunchedEffect(cameraPermissionState.shouldShowRationale) {
                cameraPermissionState.launchPermissionRequest()
            }
        },
        permissionNotAvailableContent = {
            CameraRationaleDialog(onDismiss = { onDismiss() })
        }
    ) {
        Dialog(onDismissRequest = { onDismiss() },
            properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Scaffold(backgroundColor = Color.Transparent) {
                Box {
                    QRCodeReader(callback = { setURL(it) })
                    CloseIconButton(onClick = { onDismiss() })
                }
            }
        }
    }

    LaunchedEffect(url) {
        if (url.isBlank()) {
            return@LaunchedEffect
        }
        val parsedURL = Uri.parse(url)
        Links.fromDetailLink(
            url = parsedURL,
            onArtwork = { id ->
                navController.navigateToArtworkDetail(id)
            }
        )
    }
}

@Composable
fun CameraRationaleDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        text = { Text(stringResource(R.string.search_qr_permission_rationale)) },
        confirmButton = {
            Button(onClick = {
                context.startActivity(Intent().apply {
                    action = ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                })
            }) {
                Text(stringResource(R.string.search_qr_permission_go_to_settings))
            }
        },
        onDismissRequest = { onDismiss() },
    )
}

@Composable
fun QRCodeReader(callback: QRCodeFoundCallback) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val viewFinderRadius = with(LocalDensity.current) { 150.dp.toPx() }
    val viewFinderBorderSize = with(LocalDensity.current) { 3.dp.toPx() }
    val viewFinderBorderRadius = with(LocalDensity.current) { 3.dp.toPx() }

    Scaffold {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            AndroidView(
                modifier = Modifier.matchParentSize(),
                factory = { viewContext ->
                    val previewView = PreviewView(viewContext)
                    val executor = ContextCompat.getMainExecutor(viewContext)

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val cameraSelector = CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                            .build()

                        val imageAnalysis = ImageAnalysis.Builder()
                            .build()
                            .apply {
                                setAnalyzer(executor, QRScanner(callback))
                            }

                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                    }, executor)
                    previewView
                },
            )
            Canvas(modifier = Modifier.fillMaxSize(), onDraw = {
                clipPath(
                    Path().apply {
                        addRoundRect(RoundRect(Rect(center, viewFinderRadius), CornerRadius(viewFinderBorderRadius)))
                    },
                    clipOp = ClipOp.Difference
                ) {
                    drawRect(SolidColor(Color.Black.copy(alpha = 0.6f)))
                }
                drawPath(
                    path = Path().apply {
                        addRoundRect(RoundRect(Rect(center, viewFinderRadius), CornerRadius(viewFinderBorderRadius)))
                    },
                    color = Color.White,
                    style = Stroke(viewFinderBorderSize),
                )
            })
        }
    }
}
