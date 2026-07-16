package edu.gvsu.art.gallery.ui.artwork.ar

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class AROverlayTrackerTest {
    @Test
    fun evictsLeastRecentlyPresentedBeyondCap() {
        val tracker = AROverlayTracker<Int>(maxActive = 2)
        tracker.markPresent("a", 1)
        tracker.markPresent("b", 2)
        tracker.markPresent("c", 3)
        assertEquals(setOf("b", "c"), tracker.overlays.keys)
    }

    @Test
    fun rePresentingKeepsAnOverlayAliveAndDoesNotDuplicate() {
        val tracker = AROverlayTracker<Int>(maxActive = 2)
        tracker.markPresent("a", 1)
        tracker.markPresent("b", 2)
        tracker.markPresent("a", 1)
        tracker.markPresent("c", 3)
        assertEquals(setOf("a", "c"), tracker.overlays.keys)
    }

    @Test
    fun absentRemovesTheOverlay() {
        val tracker = AROverlayTracker<Int>(maxActive = 4)
        tracker.markPresent("a", 1)
        tracker.markAbsent("a")
        assertFalse(tracker.overlays.containsKey("a"))
    }
}
