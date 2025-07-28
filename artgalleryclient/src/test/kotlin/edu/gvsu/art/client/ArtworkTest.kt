package edu.gvsu.art.client

import java.net.URL
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ArtworkTest {
    @Test
    fun test_formatArtistName() {
        val artwork = Artwork(artistName = "Nick Cave;Bob Faust")
        assertEquals(expected = "Nick Cave, Bob Faust", actual = artwork.formattedArtistName)
    }

    @Test
    fun test_videoLinks() {
        val videoLink = URL("https://example.com/video.mp4")
        val imageLink = URL("https://example.com/photo.jpg")

        val artwork = Artwork(mediaRepresentations = listOf(videoLink, imageLink))

        assertContentEquals(expected = listOf(videoLink), actual = artwork.videoLinks)
    }

    @Test
    fun test_imageLinks() {
        val videoLink = URL("https://example.com/video.mp4")
        val imageLink = URL("https://example.com/photo.jpg")

        val artwork = Artwork(mediaRepresentations = listOf(videoLink, imageLink))

        assertContentEquals(expected = listOf(imageLink), actual = artwork.imageLinks)
    }
}
