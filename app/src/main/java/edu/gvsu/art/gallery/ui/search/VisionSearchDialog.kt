package edu.gvsu.art.gallery.ui.search

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionRequired
import com.google.accompanist.permissions.rememberPermissionState
import edu.gvsu.art.gallery.ui.CloseIconButton

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VisionSearchDialog(onCapture: (Uri) -> Unit) {
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    PermissionRequired(
        permissionState = cameraPermissionState,
        permissionNotGrantedContent = {
            LaunchedEffect(cameraPermissionState.shouldShowRationale) {
                cameraPermissionState.launchPermissionRequest()
            }
        },
        permissionNotAvailableContent = {
            CameraRationaleDialog(onDismiss = { })
        }
    ) {
        Dialog(
            onDismissRequest = {  },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Scaffold(containerColor = Color.Transparent) { paddingValues ->
                Box(Modifier.padding(paddingValues)) {
                    VisionSearchCamera(onImageCaptured = onCapture)
                }
            }
        }
    }
}