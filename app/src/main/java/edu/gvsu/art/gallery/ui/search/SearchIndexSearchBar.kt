package edu.gvsu.art.gallery.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.gvsu.art.gallery.R
import edu.gvsu.art.gallery.ui.theme.ArtGalleryTheme

@ExperimentalComposeUiApi
@Composable
fun SearchIndexSearchBar(
    query: String,
    selectedCategory: SearchCategory,
    setQuery: (String) -> Unit,
    setCategory: (SearchCategory) -> Unit,
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
                            IconButton(
                                onClick = { onSelectVisionSearch() }
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = stringResource(R.string.vision_search_button),
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
            onSelectVisionSearch = {},
        )
    }
}
