package edu.gvsu.art.gallery.ui

import androidx.annotation.StringRes
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.google.accompanist.pager.*
import edu.gvsu.art.client.Artwork
import edu.gvsu.art.gallery.DetailDivider
import edu.gvsu.art.gallery.R
import edu.gvsu.art.gallery.extensions.openGoogleMaps
import edu.gvsu.art.gallery.lib.Async
import edu.gvsu.art.gallery.lib.MediaTypes
import edu.gvsu.art.gallery.navigateToArtistDetail
import edu.gvsu.art.gallery.navigateToArtworkDetail
import edu.gvsu.art.gallery.ui.foundation.rememberRemoteImage
import edu.gvsu.art.gallery.ui.theme.ArtAtGVSUTheme
import java.net.URL


@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@ExperimentalPagerApi
@Composable
fun ArtworkDetailScreen(navController: NavController, artworkID: String?) {
    artworkID ?: return Column {}
    val (isFavorite, toggleFavorite) = useFavorite(artworkID = artworkID)
    val (data) = useArtwork(id = artworkID)

    val (artwork, loading) = when (data) {
        is Async.Success -> Pair(data(), false)
        is Async.Loading -> Pair(Artwork(), true)
        else -> Pair(Artwork(), false)
    }

    ArtworkView(
        navController = navController,
        artwork = artwork,
        loading = loading,
        isFavorite = isFavorite,
        toggleFavorite = toggleFavorite,
    )
}

@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@ExperimentalPagerApi
@Composable
fun ArtworkView(
    navController: NavController,
    artwork: Artwork,
    loading: Boolean,
    isFavorite: Boolean,
    toggleFavorite: () -> Unit,
) {
    val isDialogVisible = rememberSaveable { mutableStateOf(false) }
    val thumbnailState = rememberPagerState()
    val dialogState = rememberPagerState()

    Column(Modifier
        .verticalScroll(rememberScrollState())
        .fillMaxSize()) {

        Box(Modifier.aspectRatio(4 / 3f)) {
            Box(Modifier
                .background(MaterialTheme.colors.surface)
                .fillMaxSize())

            if (!loading) {
                ArtworkMediaPager(
                    artwork = artwork,
                    pagerState = thumbnailState,
                    navigateToMedia = { isDialogVisible.value = true }
                )
            }

            CloseIconButton(style = CloseIconStyle.Back) {
                navController.popBackStack()
            }
        }

        if (!loading) {
            ArtworkDetailBody(
                navController = navController,
                artwork = artwork,
                isFavorite = isFavorite,
                toggleFavorite = toggleFavorite,
            )
        } else {
            ArtworkLoading()
        }
    }

    if (isDialogVisible.value) {
        ArtworkMediaDialog(
            artwork = artwork,
            pagerState = dialogState,
            selectedPage = thumbnailState.currentPage,
            onDismiss = { isDialogVisible.value = false }
        )
    }

    LaunchedEffect(dialogState.currentPage) {
        thumbnailState.scrollToPage(dialogState.currentPage)
    }
}

@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@ExperimentalPagerApi
@Composable
fun ArtworkMediaPager(
    artwork: Artwork,
    pagerState: PagerState,
    navigateToMedia: () -> Unit = {},
) {
    val mediaURLs = artwork.mediaRepresentations

    Box {
        HorizontalPager(
            count = mediaURLs.size,
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
        if (mediaURLs.size > 1) {
            HorizontalPagerIndicator(
                pagerState = pagerState,
                activeColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
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
    val currentTab = LocalTabScreen.current

    val context = LocalContext.current
    val descriptionRows = artwork.asDescriptionRows
    fun navigateToArtwork(artworkID: String) {
        navController.navigateToArtworkDetail(currentTab, artworkID)
    }

    Box(modifier = Modifier
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
        Image(
            painter = rememberRemoteImage(url = url),
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
                .background(Brush.verticalGradient(
                    0f to Color.Transparent,
                    1.0f to Color.Black.copy(alpha = 0.4f)
                ))
        )
    }
}

@Composable
private fun ArtistNameRow(artwork: Artwork, onClick: () -> Unit = {}) {
    if (artwork.formattedArtistName.isBlank()) {
        return
    }

    Box(
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
    }
}

@Composable
private fun DetailTextRow(row: ArtworkRow) {
    Column(Modifier.padding(vertical = 8.dp)) {
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
        text = stringResource(title), style = MaterialTheme.typography.h4,
        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
private fun ArtworkLoading() {
    LoadingView(progressAlignment = Alignment.TopCenter)
}

private val Artwork.asDescriptionRows: List<ArtworkRow>
    get() {
        return listOf(
            ArtworkRow(title = R.string.artwork_detail_work_description,
                description = workDescription),
            ArtworkRow(title = R.string.artwork_detail_historical_context,
                description = historicalContext),
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

    ArtAtGVSUTheme {
        DetailTextRow(ArtworkRow(R.string.artwork_detail_work_medium, longText))
    }
}
