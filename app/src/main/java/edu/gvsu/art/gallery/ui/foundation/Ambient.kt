package edu.gvsu.art.gallery.ui.foundation

import androidx.compose.runtime.compositionLocalOf
import edu.gvsu.art.gallery.TabScreen
import edu.gvsu.art.gallery.lib.VideoPool

val LocalTabScreen = compositionLocalOf<TabScreen> { TabScreen.Browse }

val LocalVideoPool = compositionLocalOf { VideoPool() }
