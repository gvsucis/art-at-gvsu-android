package edu.gvsu.art.client.repository

import junit.framework.TestCase.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertFalse

class CachingTest {
    @Test
    fun isFreshCache_nullValue() {
        assertFalse(isFreshCache(cacheDateTime = null))
    }

    @Test
    fun isFreshCache_oldDate() {
        assertFalse(isFreshCache(cacheDateTime = "2024-06-11 21:56:32"))
    }

    @Test
    fun isFreshCache_validDate() {
        val now = LocalDateTime.now().minusHours(1)

        assertTrue(isFreshCache(cacheDateTime = now.format(CACHE_TIME_FORMAT)))
    }
}
