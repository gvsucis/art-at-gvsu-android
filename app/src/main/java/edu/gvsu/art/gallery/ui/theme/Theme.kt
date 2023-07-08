package edu.gvsu.art.gallery.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import edu.gvsu.art.gallery.ui.Theme
import edu.gvsu.art.gallery.ui.useSettingsTheme

private val DarkColorPalette = darkColors(
    primary = LightBlue,
    secondary = LakerBlue,
    background = DarkGrey,
    onSurface = OffWhite,
    surface = Black
)

private val LightColorPalette = lightColors(
    primary = LakerBlue,
    primaryVariant = DarkGreySecondary,
    secondary = DarkBlue,
    background = OffWhite,
    onSurface = DarkGrey,
    surface = OffWhiteSecondary,
    /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */
)

@Composable
fun ArtAtGVSUTheme(darkTheme: Boolean = isAppInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
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
