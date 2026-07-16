package edu.gvsu.art.gallery.lib

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File

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
    private val mutex = Mutex()
    private val inFlight = mutableMapOf<String, Deferred<File?>>()

    suspend fun localFile(url: String): File? {
        // Register (or join) the in-flight download synchronously under the lock, so a
        // second caller arriving mid-download joins the same job instead of starting its own.
        val deferred = mutex.withLock {
            inFlight[url] ?: scope.async(Dispatchers.IO) { fetch(url) }.also { inFlight[url] = it }
        }

        try {
            return deferred.await()
        } finally {
            mutex.withLock {
                if (inFlight[url] === deferred) {
                    inFlight.remove(url)
                }
            }
        }
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
