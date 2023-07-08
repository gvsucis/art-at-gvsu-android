package edu.gvsu.art.client

import org.junit.Test
import kotlin.test.assertEquals

internal class ArtworkTest {
    @Test
    fun test_formatArtistName() {
        val artwork = Artwork(artistName = "Nick Cave;Bob Faust")
        assertEquals(expected =  "Nick Cave, Bob Faust", actual = artwork.formattedArtistName)
    }
}
