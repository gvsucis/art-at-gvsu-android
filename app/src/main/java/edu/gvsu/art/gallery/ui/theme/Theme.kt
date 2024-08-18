package edu.gvsu.art.gallery.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import edu.gvsu.art.gallery.ui.Theme
import edu.gvsu.art.gallery.ui.useSettingsTheme

private val DarkColorPalette = darkColorScheme(
    primary = LightBlue,
    secondary = LakerBlue,
    background = DarkGrey,
    onSurface = OffWhite,
    surface = Black,
)

private val LightColorPalette = lightColorScheme(
    primary = LakerBlue,
    secondary = DarkBlue,
    background = OffWhite,
    onSurface = DarkGrey,
    surface = OffWhiteSecondary,
)

@Composable
fun ArtGalleryTheme(darkTheme: Boolean = isAppInDarkTheme(), content: @Composable () -> Unit) {
    val colorScheme = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.navigationBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

@Composable
fun isAppInDarkTheme(): Boolean {
    val (theme) = useSettingsTheme()
    return when (theme) {
        Theme.Dark -> true
        Theme.Light -> false
        Theme.SystemDefault -> isSystemInDarkTheme()
    }
}
