package edu.gvsu.art.gallery.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.gvsu.art.gallery.R
import edu.gvsu.art.gallery.ui.theme.ArtGalleryTheme

@ExperimentalComposeUiApi
@Composable
fun SearchIndexSearchBar(
    query: String,
    selectedModel: SearchModel,
    setQuery: (String) -> Unit,
    setModel: (SearchModel) -> Unit,
    selectQRScanner: () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val radioOptions = listOf(SearchModel.ARTIST, SearchModel.ARTWORK)
    Surface(
        elevation = 4.dp,
    ) {
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
                            tint = MaterialTheme.colors.onSurface,
                        )
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { setQuery("") }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = null,
                                    tint = MaterialTheme.colors.onSurface,
                                )
                            }
                        } else {
                            IconButton(
                                onClick = { selectQRScanner() }
                            ) {
                                Icon(
                                    Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    tint = MaterialTheme.colors.onSurface,
                                )
                            }
                        }
                    },
                    placeholder = { Text(stringResource(R.string.search_index_bar_placeholder)) },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = MaterialTheme.colors.onSurface,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { keyboardController?.hide() }
                    )
                )
            }
            Row(
                modifier = Modifier
                    .selectableGroup()
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                radioOptions.forEach { model ->
                    Row(
                        Modifier
                            .selectable(
                                selected = (model == selectedModel),
                                onClick = { setModel(model) },
                                role = Role.RadioButton
                            )
                            .clip(MaterialTheme.shapes.large)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = (model == selectedModel),
                            onClick = null
                        )
                        Text(
                            text = model.localized(),
                            style = MaterialTheme.typography.body1.merge(),
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }
        }
    }
}


@ExperimentalComposeUiApi
@Composable
@Preview
fun PreviewSearchBar() {
    ArtGalleryTheme {
        SearchIndexSearchBar(
            query = "",
            selectedModel = SearchModel.ARTIST,
            setQuery = {},
            setModel = {},
            selectQRScanner = {},
        )
    }
}
