package edu.gvsu.art.gallery.ui

import androidx.compose.runtime.compositionLocalOf
import edu.gvsu.art.gallery.TabScreen

val LocalTabScreen = compositionLocalOf<TabScreen> { TabScreen.Browse }
