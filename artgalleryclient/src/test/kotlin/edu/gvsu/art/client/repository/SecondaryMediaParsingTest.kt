package edu.gvsu.art.client.repository

import edu.gvsu.art.client.SecondaryMedia
import java.net.URL
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SecondaryMediaParsingTest  {
    val secondaryMediaReps = "https://example.com/first.mp4;https://example.com/second.mp4"
    val secondaryMediaRepThumbnails = "https://example.com/first.jpg;https://example.com/second.jpg"

    @Test
    fun `it returns parsed media`() {
        val expected = listOf(
            SecondaryMedia(
                url = URL("https://example.com/first.mp4"),
                thumbnailURL = URL("https://example.com/first.jpg"),
            ),
            SecondaryMedia(
                url = URL("https://example.com/second.mp4"),
                thumbnailURL = URL("https://example.com/second.jpg"),
            )
        )

        val result = parseSecondaryMedia(secondaryMediaReps, secondaryMediaRepThumbnails)

        assertEquals(expected = result, actual = expected)
    }

    @Test
    fun `returns empty for mismatched lists`() {
        val result = parseSecondaryMedia(secondaryMediaReps, "https://example.com/first.jpg")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `returns empty for blank lists`() {
        val result = parseSecondaryMedia(null, "")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `returns empty malformed URLS`() {
        val result = parseSecondaryMedia(secondaryMediaReps, "bad-url;second-bad-url")

        assertTrue(result.isEmpty())
    }
}