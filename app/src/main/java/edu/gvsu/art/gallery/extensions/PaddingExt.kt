package edu.gvsu.art.gallery.extensions

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

fun Modifier.nestedScaffoldPadding(paddingValues: PaddingValues) = padding(top = paddingValues.calculateTopPadding())
