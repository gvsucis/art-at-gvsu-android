package edu.gvsu.art.client.common

import org.junit.Test
import org.junit.Assert.assertEquals

class MediaTypesTest {
    @Test
    fun `it tests a URL ending without an extension`() {
        val result = MediaTypes.getFileExtensionFromUrl("https://example.com/file")
        assertEquals("", result)
    }

    @Test
    fun `it tests a URL ending with an extension`() {
        val result = MediaTypes.getFileExtensionFromUrl("https://example.com/video.mp4")
        assertEquals("mp4", result)
    }

    @Test
    fun `it tests a URL ending with an extension and a query param`() {
        val result = MediaTypes.getFileExtensionFromUrl("https://example.com/video.mp4?token=abc123")
        assertEquals("mp4", result)
    }
}