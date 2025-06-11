package edu.gvsu.art.gallery.ui.artwork.detail

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import edu.gvsu.art.client.Artwork
import edu.gvsu.art.gallery.DetailDivider
import edu.gvsu.art.gallery.R
import edu.gvsu.art.gallery.extensions.nestedScaffoldPadding
import edu.gvsu.art.gallery.extensions.openGoogleMaps
import edu.gvsu.art.gallery.lib.MediaTypes
import edu.gvsu.art.gallery.navigateToArtistDetail
import edu.gvsu.art.gallery.navigateToArtworkDetail
import edu.gvsu.art.gallery.ui.ArtworkVideoPlaceholder
import edu.gvsu.art.gallery.ui.CloseIconButton
import edu.gvsu.art.gallery.ui.CloseIconStyle
import edu.gvsu.art.gallery.ui.LoadingView
import edu.gvsu.art.gallery.ui.MapSnapshot
import edu.gvsu.art.gallery.ui.RelatedArtworks
import edu.gvsu.art.gallery.ui.foundation.LocalTopLevelRoute
import edu.gvsu.art.gallery.ui.mediaviewer.LocalMediaViewerState
import edu.gvsu.art.gallery.ui.theme.ArtGalleryTheme
import org.koin.androidx.compose.koinViewModel
import java.net.URL


@ExperimentalComposeUiApi
@Composable
fun ArtworkDetailScreen(
    navController: NavController,
    viewModel: ArtworkDetailViewModel = koinViewModel()
) {
    val artwork = viewModel.artwork

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
    ) { padding ->
        Box(
            Modifier
                .nestedScaffoldPadding(padding)
                .fillMaxSize()
        ) {
            if (artwork == null) {
                LoadingView(progressIndicatorDelay = 500)
            } else {
                ArtworkView(
                    navController = navController,
                    artwork = artwork,
                    isFavorite = viewModel.isFavorite,
                    toggleFavorite = viewModel::toggleFavorite,
                )
            }
        }
    }
}

@ExperimentalComposeUiApi
@Composable
fun ArtworkView(
    navController: NavController,
    artwork: Artwork,
    isFavorite: Boolean,
    toggleFavorite: () -> Unit,
) {
    val mediaViewer = LocalMediaViewerState.current
    val mediaURLs = artwork.mediaRepresentations
    val thumbnailState = rememberPagerState(initialPage = 0) {
        mediaURLs.size
    }

    Column(
        Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
    ) {
        Box(Modifier.aspectRatio(4 / 3f)) {
            Box(
                Modifier
                    .background(colorScheme.surface)
                    .fillMaxSize()
            )

            ArtworkMediaPager(
                artwork,
                mediaURLs = mediaURLs,
                pagerState = thumbnailState,
                navigateToMedia = {
                    mediaViewer.present(
                        artwork,
                        currentIndex = thumbnailState.currentPage
                    )
                }
            )

            CloseIconButton(style = CloseIconStyle.Back) {
                navController.popBackStack()
            }
        }

        ArtworkDetailBody(
            navController = navController,
            artwork = artwork,
            isFavorite = isFavorite,
            toggleFavorite = toggleFavorite,
        )
    }

    LaunchedEffect(mediaViewer.currentIndex) {
        if (mediaViewer.artwork != null) {
            thumbnailState.scrollToPage(mediaViewer.currentIndex)
        }
    }
}

@ExperimentalComposeUiApi
@Composable
fun ArtworkMediaPager(
    artwork: Artwork,
    mediaURLs: List<URL>,
    pagerState: PagerState,
    navigateToMedia: () -> Unit = {},
) {
    Box {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.aspectRatio(4 / 3f),
            key = { mediaURLs[it] }
        ) { page ->
            val url = mediaURLs[page]

            Box(modifier = Modifier.clickable(onClick = { navigateToMedia() })) {
                if (MediaTypes.isVideo(url)) {
                    ArtworkVideoPlaceholder(url = artwork.mediaSmall)
                } else {
                    PagerImage(url = url)
                }
            }
        }
        Row(
            Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            if (mediaURLs.size > 1) {
                HorizontalPagerIndicator(pagerState = pagerState)
            }
        }
    }
}

@Composable
fun ArtworkDetailBody(
    navController: NavController,
    artwork: Artwork,
    isFavorite: Boolean,
    toggleFavorite: () -> Unit,
) {
    val currentTab = LocalTopLevelRoute.current

    val context = LocalContext.current
    val descriptionRows = artwork.asDescriptionRows
    fun navigateToArtwork(artworkID: String) {
        navController.navigateToArtworkDetail(currentTab, artworkID)
    }

    Box(
        modifier = Modifier
            .padding(top = 16.dp)
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp)
    ) {
        ArtworkDetailTitleRow(
            artwork = artwork,
            isFavorite = isFavorite,
            toggleFavorite = toggleFavorite
        )
    }
    DetailDivider()
    ArtistNameRow(artwork = artwork) {
        navController.navigateToArtistDetail(currentTab, artwork.artistID)
    }
    DetailDivider()
    descriptionRows.forEachIndexed { index, row ->
        DetailTextRow(row = row)
        if (index != descriptionRows.lastIndex) {
            DetailDivider()
        }
    }
    artwork.locationGeoreference?.let { location ->
        DetailDivider()
        Spacer(Modifier.height(8.dp))
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            MapSnapshot(
                location = location,
                zoom = 16f,
                modifier = Modifier
                    .height(200.dp)
                    .clickable {
                        context.openGoogleMaps(location, artwork.name)
                    }
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(3.dp))
            )
        }
    }
    if (artwork.relatedWorks.isNotEmpty()) {
        Spacer(Modifier.height(8.dp))
        DetailDivider()
        Box(modifier = Modifier.padding(vertical = 8.dp)) {
            DetailTitle(title = R.string.artwork_detail_related_works)
        }
        RelatedArtworks(
            artworks = artwork.relatedWorks,
            onArtworkSelect = { artworkID ->
                navigateToArtwork(artworkID)
            }
        )
    }
    Spacer(Modifier.height(16.dp))
}

@Composable
fun PagerImage(url: URL) {
    Box {
        AsyncImage(
            model = url.toString(),
            contentScale = ContentScale.Crop,
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .fillMaxHeight(fraction = 0.5f)
                .clip(RectangleShape)
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        1.0f to Color.Black.copy(alpha = 0.4f)
                    )
                )
        )
    }
}

@Composable
private fun ArtistNameRow(artwork: Artwork, onClick: () -> Unit = {}) {
    if (artwork.formattedArtistName.isBlank()) {
        return
    }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        DetailTextRow(
            ArtworkRow(
                title = R.string.artwork_detail_artist,
                description = artwork.formattedArtistName
            )
        )

        Spacer(Modifier.width(16.dp))
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            modifier = Modifier.padding(end = 16.dp)
        )
    }
}

@Composable
private fun DetailTextRow(row: ArtworkRow) {
    Column(
        Modifier.padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        DetailTitle(title = row.title)
        Text(
            row.description,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun DetailTitle(@StringRes title: Int) {
    Text(
        text = stringResource(title), style = MaterialTheme.typography.headlineMedium,
        color = colorScheme.onSurface.copy(alpha = 0.6f),
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

private val Artwork.asDescriptionRows: List<ArtworkRow>
    get() {
        return listOf(
            ArtworkRow(
                title = R.string.artwork_detail_work_description,
                description = workDescription
            ),
            ArtworkRow(
                title = R.string.artwork_detail_historical_context,
                description = historicalContext
            ),
            ArtworkRow(title = R.string.artwork_detail_work_medium, description = workMedium),
            ArtworkRow(title = R.string.artwork_detail_work_date, description = workDate),
            ArtworkRow(title = R.string.artwork_detail_location, description = location),
            ArtworkRow(title = R.string.artwork_detail_identifier, description = identifier),
            ArtworkRow(title = R.string.artwork_detail_credit_line, description = creditLine),
        ).filterNot { it.description.isBlank() }
    }

data class ArtworkRow(@StringRes val title: Int, val description: String)

@Composable
@Preview("Detail row with long text")
fun PreviewDetailTextRow() {
    val longText =
        "Judith Brown employed scrap metal in much of her work, ranging from small religious ceremonial objects such as a Hanukkah lamp in the collection of The Jewish Museum, to monumental sculptures and public art projects like the installation commissioned for the Federal Courthouse building in Trenton, New Jersey."

    ArtGalleryTheme {
        DetailTextRow(ArtworkRow(R.string.artwork_detail_work_medium, longText))
    }
}
