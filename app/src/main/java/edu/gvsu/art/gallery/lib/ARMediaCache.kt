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
 * least-recently-used eviction so the gallery's videos and models never all sit
 * on disk at once. Mirrors the iOS `ARMediaCache`. Holds only replayable media
 * (videos and 3D models); reference images seed ARCore's image database once and
 * are fetched outside this cache.
 *
 * Concurrent callers for the same URL share a single download (in-flight dedup)
 * instead of racing past [FileDownloader]'s file-exists check and each starting
 * the same download.
 *
 * @param directory      the LRU-managed cache directory (e.g. `cacheDir/ar_assets`).
 * @param scope          scope the shared downloads run in (typically `viewModelScope`).
 * @param maxCachedFiles cap on cached files before least-recently-used eviction.
 */
class ARMediaCache(
    private val directory: File,
    private val scope: CoroutineScope,
    private val maxCachedFiles: Int = 8,
) {
    private val mutex = Mutex()
    private val inFlight = mutableMapOf<String, Deferred<File?>>()

    /**
     * A local file for [url], downloading it if it isn't already cached. Re-touches
     * the file so the most-recently-used media survives eviction. Returns null on
     * download failure.
     */
    suspend fun localFile(url: String): File? {
        // Register (or join) the in-flight download synchronously under the lock, so a
        // second caller arriving mid-download joins the same job instead of starting its own.
        val deferred = mutex.withLock {
            inFlight[url] ?: scope.async(Dispatchers.IO) { fetch(url) }.also { inFlight[url] = it }
        }
        return try {
            deferred.await()
        } finally {
            mutex.withLock { if (inFlight[url] === deferred) inFlight.remove(url) }
        }
    }

    private suspend fun fetch(url: String): File? {
        // FileDownloader already returns the cached file when present (MD5-named).
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
