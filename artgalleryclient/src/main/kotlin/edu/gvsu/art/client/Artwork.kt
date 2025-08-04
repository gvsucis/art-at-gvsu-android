package edu.gvsu.art.client

import android.os.Parcelable
import edu.gvsu.art.client.common.MediaTypes
import kotlinx.parcelize.Parcelize
import okhttp3.MediaType
import java.net.URL

@Parcelize
data class Artwork(
    val id: String = "",
    val isPublic: Boolean = true,
    val mediaRepresentations: List<URL> = listOf(),
    val name: String = "",
    val artistID: String = "",
    private val artistName: String = "",
    val historicalContext: String = "",
    val workDescription: String = "",
    val workDate: String = "",
    val workMedium: String = "",
    val locationID: String = "",
    val location: String = "",
    val identifier: String = "",
    val creditLine: String = "",
    val locationGeoreference: LatLng? = null,
    val relatedWorks: List<Artwork> = listOf(),
    val mediaSmall: URL? = null,
    val mediaMedium: URL? = null,
    val mediaLarge: URL? = null,
    val thumbnail: URL? = null,
    val arDigitalAssetURL: URL? = null,
) : Parcelable {
    val hasAR: Boolean
        get() = arDigitalAssetURL != null

    val imageLinks: List<URL>
        get() = mediaRepresentations.filterNot { MediaTypes.isVideo(it) }

    val videoLinks: List<URL>
        get() = mediaRepresentations.filter { MediaTypes.isVideo(it) }

    val formattedArtistName: String
        get() {
            return if (artistName.isNotBlank()) {
                artistName.split(";").joinToString(", ")
            } else {
                artistName
            }
        }
}
