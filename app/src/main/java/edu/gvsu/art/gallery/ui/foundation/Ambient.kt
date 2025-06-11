package edu.gvsu.art.gallery.ui.foundation

import androidx.compose.runtime.compositionLocalOf
import edu.gvsu.art.gallery.TopLevelRoute
import edu.gvsu.art.gallery.lib.VideoPool

val LocalTopLevelRoute = compositionLocalOf<TopLevelRoute> { TopLevelRoute.Browse }

val LocalVideoPool = compositionLocalOf { VideoPool() }
