package edu.gvsu.art.gallery.ui.search

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.gvsu.art.gallery.R
import edu.gvsu.art.gallery.lib.createTempImage
import edu.gvsu.art.gallery.ui.theme.ArtGalleryTheme
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@ExperimentalComposeUiApi
@Composable
fun SearchIndexSearchBar(
    query: String,
    selectedCategory: SearchCategory,
    setQuery: (String) -> Unit,
    setCategory: (SearchCategory) -> Unit,
    onVisionSearchImageResult: (uri: Uri) -> Unit,
    onSelectQRScanner: () -> Unit,
    onSelectVisionSearch: () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val options = listOf(SearchCategory.ARTIST, SearchCategory.ARTWORK)

    Column {
        Row {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = query,
                onValueChange = { setQuery(it) },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { setQuery("") }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
//                            VisionSearchButton(
//                                onResult = { uri ->
//                                    onVisionSearchImageResult(uri)
//                                }
//                            )
                            IconButton(
                                onClick = { onSelectVisionSearch() }
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                            IconButton(
                                onClick = { onSelectQRScanner() }
                            ) {
                                Icon(
                                    Icons.Default.QrCodeScanner,
                                    contentDescription = stringResource(R.string.scan_qr_code_button),
                                    tint = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                },
                placeholder = { Text(stringResource(R.string.search_index_bar_placeholder)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                ),
                keyboardActions = KeyboardActions(
                    onDone = { keyboardController?.hide() }
                )
            )
        }
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            options.forEachIndexed { index, label ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = options.size
                    ),
                    onClick = { setCategory(label) },
                    selected = label == selectedCategory
                ) {
                    Text(stringResource(label.title))
                }
            }
        }
    }
}

@Composable
fun VisionSearchButton(onResult: (uri: Uri) -> Unit) {
    val context = LocalContext.current
    var uri by rememberSaveable { mutableStateOf(value = Uri.EMPTY) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                onResult(uri)
            }
        }
    )

    val permissions = rememberLauncherForActivityResult(RequestPermission()) { _ ->
        uri = context.createTempImage()
        takePictureLauncher.launch(uri)
    }

    IconButton(
        onClick = {
            permissions.launch(Manifest.permission.CAMERA)
        }
    ) {
        Icon(
            Icons.Default.PhotoCamera,
            contentDescription = stringResource(R.string.vision_search_button),
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun saveBitmapToCache(bitmap: Bitmap, context: Context): File? {
    val cacheDir = context.cacheDir
    val fileName = "vision_search_${System.currentTimeMillis()}.jpg"
    val file = File(cacheDir, fileName)

    try {
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        return file
    } catch (e: IOException) {
        return null
    }
}

@ExperimentalComposeUiApi
@Composable
@Preview
fun SearchBarPreview() {
    ArtGalleryTheme {
        SearchIndexSearchBar(
            query = "",
            selectedCategory = SearchCategory.ARTIST,
            setQuery = {},
            setCategory = {},
            onSelectQRScanner = {},
            onVisionSearchImageResult = {},
            onSelectVisionSearch = {},
        )
    }
}
