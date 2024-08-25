package edu.gvsu.art.gallery.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import edu.gvsu.art.gallery.BuildConfig
import edu.gvsu.art.gallery.R
import edu.gvsu.art.gallery.extensions.nestedScaffoldPadding
import edu.gvsu.art.gallery.ui.theme.ArtGalleryTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            GalleryTopAppBar(
                title = stringResource(R.string.navigation_Settings),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .nestedScaffoldPadding(padding)
                .fillMaxSize()
                .verticalScroll(state = scrollState)
        ) {
            SettingsContent()
        }
    }

}

@Composable
private fun SettingsContent() {
    AboutView()
    HorizontalDivider()
    AppearanceView()
    HorizontalDivider()
    ExternalLinksView()
    HorizontalDivider()
    BuildInfoView()
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun AboutView() {
    SettingsColumn(title = stringResource(R.string.settings_about_title)) {
        Text(
            stringResource(R.string.settings_about_description),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun AppearanceView() {
    val (theme, setTheme) = useSettingsTheme()

    val radioOptions = mapOf(
        Theme.Light to R.string.settings_theme_light,
        Theme.Dark to R.string.settings_theme_dark,
        Theme.SystemDefault to R.string.settings_theme_system_default,
    )

    SettingsColumn(stringResource(R.string.settings_appearance_title)) {
        Column(
            modifier = Modifier
                .selectableGroup()
                .fillMaxWidth(),
        ) {
            radioOptions.forEach { (option, text) ->
                Row(
                    Modifier
                        .selectable(
                            selected = theme == option,
                            onClick = { setTheme(option) },
                            role = Role.RadioButton
                        )
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = theme == option,
                        onClick = null,
                    )
                    Text(
                        text = stringResource(text),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ExternalLinksView() {
    SettingsColumn {
        ExternalLink(
            linkText = stringResource(R.string.settings_art_gallery_link),
            Uri.parse("https://www.gvsu.edu/artgallery/")
        )
        ExternalLink(
            linkText = stringResource(R.string.settings_more_apps_link),
            Uri.parse("https://play.google.com/store/apps/developer?id=GVSU+School+of+Computing")
        )
        ExternalLink(
            linkText = stringResource(R.string.settings_aci_link),
            Uri.parse("http://aci.cis.gvsu.edu/")
        )
    }
}

@Composable
fun ExternalLink(linkText: String, link: Uri) {
    val context = LocalContext.current

    fun viewLink() {
        context.startActivity(Intent(Intent.ACTION_VIEW, link))
    }

    TextButton(onClick = { viewLink() }) {
        Text(linkText)
    }
}

@Composable
private fun BuildInfoView() {
    val text = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
    SettingsColumn(title = stringResource(R.string.settings_section_title_build_info)) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}


@Composable
private fun SettingsColumn(
    title: String = "",
    section: @Composable () -> Unit,
) {
    Column {
        if (title.isNotBlank()) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp)
            )
        }
        Column(Modifier.padding(start = 16.dp, bottom = 8.dp)) {
            section()
        }
    }
}

@Preview
@Composable
fun PreviewSettingsContent() {
    ArtGalleryTheme {
        Column {
            SettingsContent()
        }
    }
}
