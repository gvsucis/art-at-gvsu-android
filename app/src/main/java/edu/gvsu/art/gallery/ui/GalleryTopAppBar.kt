package edu.gvsu.art.gallery.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp

@Composable
fun GalleryTopAppBar(
    title: String = "",
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    elevation: Dp = AppBarDefaults.TopAppBarElevation
) {
    TopAppBar(
        title = { Text(title) },
        backgroundColor = MaterialTheme.colors.surface,
        navigationIcon = navigationIcon,
        actions = actions,
        elevation = elevation,
    )
}
