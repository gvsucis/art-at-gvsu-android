package edu.gvsu.art.gallery.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import edu.gvsu.art.gallery.extensions.dataStore
import kotlinx.coroutines.launch


@Composable
fun useSettingsTheme(): Pair<Theme, (theme: Theme) -> Any> {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val updateTheme = { theme: Theme ->
        coroutineScope.launch {
            context.dataStore.edit { settings ->
                settings[THEME_SETTING] = theme.value
            }
        }
    }

    val data = context.dataStore.data.collectAsState(null)
    val settings = data.value ?: return Pair(Theme.SystemDefault, updateTheme)
    val theme = settings[THEME_SETTING] ?: SYSTEM_DEFAULT

    return Pair(Theme.from(theme), updateTheme)
}

sealed class Theme(val value: String) {
    object Light : Theme(value = LIGHT)
    object Dark : Theme(value = DARK)
    object SystemDefault : Theme(value = SYSTEM_DEFAULT)

    companion object {
        fun from(value: String): Theme {
            return when (value) {
                LIGHT -> Light
                DARK -> Dark
                else -> SystemDefault
            }
        }
    }
}

const val LIGHT = "light"
const val DARK = "dark"
const val SYSTEM_DEFAULT = "system_default"

val THEME_SETTING = stringPreferencesKey("theme_setting")
