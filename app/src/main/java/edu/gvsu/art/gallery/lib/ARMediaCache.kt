package edu.gvsu.art.gallery.lib

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Downloads AR media on demand and caches it on disk, bounding the cache with
 * LRU eviction so the gallery's videos and models never all sit
 * on disk at once. Holds only replayable media (videos and 3D models).
 * Reference images populate ARCore's image database once and
 * are fetched outside this cache.
 *
 * Concurrent callers for the same URL share a single download (in-flight de-duplication)
 * instead of racing past [FileDownloader]'s file-exists check and each starting
 * the same download.
 *
 * Mirrors the iOS [ARMediaCache](https://github.com/gvsucis/art-at-gvsu-ios/blob/9a0ba5c41ca0e3909dd24b76d226c9611ded4e02/ArtAtGVSU/AR/ARMediaCache.swift).
 */
class ARMediaCache(
    private val directory: File,
    private val scope: CoroutineScope,
    private val maxCachedFiles: Int = 8,
) {
    private val inFlight = ConcurrentHashMap<String, Deferred<File?>>()

    suspend fun localFile(url: String): File? {
        val deferred = inFlight.computeIfAbsent(url) {
            scope.async(Dispatchers.IO) { fetch(url) }.also { download ->
                download.invokeOnCompletion { inFlight.remove(url, download) }
            }
        }
        return deferred.await()
    }

    private suspend fun fetch(url: String): File? {
        // FileDownloader already returns the cached file when present.
        val file = FileDownloader.download(url, directory).getOrNull() ?: return null
        file.setLastModified(System.currentTimeMillis())
        prune()
        return file
    }

    /** Drops the least-recently-touched files once the cache exceeds its cap. */
    private fun prune() {
        val files = directory.listFiles()?.sortedBy { it.lastModified() } ?: return
        if (files.size <= maxCachedFiles) return
        files.take(files.size - maxCachedFiles).forEach { it.delete() }
    }
}
