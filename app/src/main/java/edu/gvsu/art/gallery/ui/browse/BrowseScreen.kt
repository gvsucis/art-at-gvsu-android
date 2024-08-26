package edu.gvsu.art.gallery.ui.browse

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import edu.gvsu.art.client.Artwork
import edu.gvsu.art.client.ArtworkCollection
import edu.gvsu.art.gallery.R
import edu.gvsu.art.gallery.Route
import edu.gvsu.art.gallery.extensions.nestedScaffoldPadding
import edu.gvsu.art.gallery.navigateToArtworkDetail
import edu.gvsu.art.gallery.navigateToCollection
import edu.gvsu.art.gallery.ui.foundation.LocalTabScreen
import edu.gvsu.art.gallery.ui.theme.OffWhite
import edu.gvsu.art.gallery.ui.theme.OffWhiteSecondary
import org.koin.androidx.compose.koinViewModel
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseScreen(
    viewModel: BrowseIndexViewModel = koinViewModel(),
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.background
                ),
                title = {},
                actions = {
                    IconButton(
                        onClick = {
                            navController.navigate(Route.Settings)
                        }
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = null,
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .nestedScaffoldPadding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.browse_featured_title),
                    style = typography.displayLarge
                )
            }
            HomeFeaturedImageView(
                viewModel.artwork,
                navController = navController,
            )

            Spacer(Modifier.height(8.dp))

            BrowseAction(text = R.string.home_featured_collection) {
                navController.navigateToCollection(ArtworkCollection.FeaturedArt)
            }

            BrowseAction(text = R.string.home_ar_collection) {
                navController.navigateToCollection(ArtworkCollection.FeaturedAR)
            }

            BrowseAction(text = R.string.home_browse_campuses) {
                navController.navigate(Route.BrowseLocationsIndex)
            }
        }
    }
}

@Composable
fun BrowseAction(@StringRes text: Int, onClick: () -> Unit) {
    Box(modifier = Modifier.clickable { onClick() }) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .fillMaxWidth()
        ) {
            Text(stringResource(text))
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null
            )
        }
    }
    HorizontalDivider(Modifier.padding(horizontal = 16.dp))
}

@Composable
fun HomeFeaturedImageView(
    currentArtwork: Artwork,
    navController: NavController,
) {
    val currentTab = LocalTabScreen.current
    fun navigateToArtwork() {
        if (currentArtwork.id.isBlank()) {
            return
        }
        navController.navigateToArtworkDetail(currentTab, currentArtwork.id)
    }
    Surface(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(10.dp))
            .aspectRatio(1f)
    ) {
        Box(
            modifier = Modifier
                .background(OffWhiteSecondary, RoundedCornerShape(size = 10.dp))
                .clickable { navigateToArtwork() }
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(currentArtwork.mediaLarge.toString())
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .aspectRatio(1f)
                    .fillMaxWidth()
            )
            FeaturedTitle(
                artwork = currentArtwork,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .aspectRatio(1f)
            )
        }
    }
}

@Composable
private fun FeaturedTitle(artwork: Artwork, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RectangleShape)
            .background(
                Brush.verticalGradient(
                    0f to Color.Transparent,
                    0.5f to Color.Transparent,
                    1.0f to Color.Black.copy(alpha = 0.6f)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                artwork.name,
                color = OffWhite,
                style = typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            if (artwork.formattedArtistName.isNotBlank()) {
                Text(
                    artwork.formattedArtistName,
                    color = OffWhite.copy(alpha = 0.8f),
                    style = typography.titleMedium
                )
            }
        }
    }
}

@Composable
@Preview
fun PreviewHomeFeaturedArtworkView() {
    val artwork = Artwork(
        id = "12853",
        mediaRepresentations = listOf(
            "https://artgallery.gvsu.edu/admin/media/collectiveaccess/images/1/4/4/5337_ca_object_representations_media_14448_large.jpg"
        ).map { URL(it) },
        name = "Duo (Two)",
        artistName = "Judith Brown",
        historicalContext = "Judith Brown employed scrap metal in much of her work, ranging from small religious ceremonial objects such as a Hanukkah lamp in the collection of The Jewish Museum, to monumental sculptures and public art projects like the installation commissioned for the Federal Courthouse building in Trenton, New Jersey.",
        workDescription = "",
        workDate = "ca. 1985",
        workMedium = "Painted and welded scrap metal",
        location = "3rd Floor (JHZ)",
        identifier = "2013.68.7",
        creditLine = "A Gift of the Stuart and Barbara Padnos Foundation",
        mediaLarge = URL("https://artgallery.gvsu.edu/admin/media/collectiveaccess/images/2/42600_ca_object_representations_media_203_large.jpg")
    )

    HomeFeaturedImageView(artwork, navController = rememberNavController())
}
