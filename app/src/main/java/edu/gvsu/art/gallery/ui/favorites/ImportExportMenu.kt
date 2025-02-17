package edu.gvsu.art.gallery.ui.favorites

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import edu.gvsu.art.gallery.R
import edu.gvsu.art.gallery.notifications.Notifications

@Composable
fun ImportExportMenu(
    onRequestImport: () -> Unit,
    onRequestExport: () -> Unit,
) {
    val (expanded, setExpanded) = remember { mutableStateOf(false) }

    val permissions = rememberLauncherForActivityResult(RequestPermission()) { _ ->
        onRequestImport()
    }

    IconButton(onClick = { setExpanded(true) }) {
        Icon(
            Icons.Default.MoreVert,
            contentDescription = stringResource(R.string.favorites_menu_title),
        )
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { setExpanded(false) },
    ) {
        DropdownMenuItem(
            text = {
                Text(text = stringResource(R.string.favorites_menu_item_import))
            },
            onClick = {
                if (Notifications.askForPermission) {
                    permissions.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    onRequestImport()
                }
                setExpanded(false)
            }
        )
        DropdownMenuItem(
            text = {
                Text(text = stringResource(R.string.favorites_menu_item_export))
            },
            onClick = {
                onRequestExport()
                setExpanded(false)
            }
        )
    }
}

@Preview
@Composable
fun FeedActionMenuPreview() {
    Column {
        ImportExportMenu(
            onRequestImport = {},
            onRequestExport = {}
        )
    }
}
