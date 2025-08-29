package edu.gvsu.art.gallery.lib

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import android.util.LruCache
import kotlinx.coroutines.*
import java.io.File
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

class ARVideoCache(
    context: Context,
    maxMemoryCacheSize: Int = 3,
    private val maxDiskCacheSize: Long = 200L * 1024 * 1024 // 200MB
) {
    data class CachedVideo(
        val localPath: String,
        val mediaPlayer: MediaPlayer?,
        val lastAccessed: Long = System.currentTimeMillis(),
        val isReady: Boolean = false
    )

    private val memoryCache = LruCache<String, CachedVideo>(maxMemoryCacheSize)
    private val downloadJobs = ConcurrentHashMap<String, Job>()
    private val cacheDirectory = File(context.cacheDir, "ar_videos")

    init {
        if (!cacheDirectory.exists()) {
            cacheDirectory.mkdirs()
        }
    }

    /**
     * Preload a video - downloads to disk and prepares MediaPlayer in memory
     */
    fun preload(artworkId: String, videoUrl: URL): Job {
        return downloadJobs.getOrPut(artworkId) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val localFile = downloadVideo(artworkId, videoUrl)

                    if (localFile.exists()) {
                        withContext(Dispatchers.Main) {
                            prepareMediaPlayer(artworkId, localFile.absolutePath)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ARVideoCache", "Error preloading video $artworkId: ${e.message}", e)
                }
            }
        }
    }

    /**
     * Get cached video instantly if available, null otherwise
     */
    fun getInstant(artworkId: String): CachedVideo? {
        val cached = memoryCache.get(artworkId)
        return if (cached?.isReady == true) {
            // Update last accessed time
            cached.copy(lastAccessed = System.currentTimeMillis()).also {
                memoryCache.put(artworkId, it)
            }
        } else {
            null
        }
    }

    suspend fun get(artworkId: String, videoUrl: URL): CachedVideo? {
        getInstant(artworkId)?.let { return it }

        downloadJobs[artworkId]?.join()

        getInstant(artworkId)?.let { return it }

        val localFile = getLocalFile(videoUrl)
        if (localFile.exists()) {
            return withContext(Dispatchers.Main) {
                prepareMediaPlayer(artworkId, localFile.absolutePath)
            }
        }

        return try {
            val downloadedFile = downloadVideo(artworkId, videoUrl)
            if (downloadedFile.exists()) {
                withContext(Dispatchers.Main) {
                    prepareMediaPlayer(artworkId, downloadedFile.absolutePath)
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("ARVideoCache", "Error downloading video $artworkId: ${e.message}", e)
            null
        }
    }

    private suspend fun downloadVideo(artworkId: String, videoUrl: URL): File {
        val localFile = getLocalFile(videoUrl)

        if (localFile.exists()) {
            return localFile
        }

        ensureDiskCacheSize()

        val result = FileDownloader.download(
            url = videoUrl.toString(),
            directory = cacheDirectory
        )

        return result.fold(
            onSuccess = { downloadedFile ->
                val targetFile = getLocalFile(videoUrl)
                if (downloadedFile.renameTo(targetFile)) {
                    targetFile
                } else {
                    downloadedFile
                }
            },
            onFailure = { error ->
                Log.e("ARVideoCache", "Failed to download video $artworkId: ${error.message}")
                throw error
            }
        )
    }

    private suspend fun prepareMediaPlayer(artworkId: String, localPath: String): CachedVideo? {
        return try {
            val mediaPlayer = MediaPlayer().apply {
                setDataSource(localPath)
                isLooping = true

                // Prepare synchronously on background thread
                withContext(Dispatchers.IO) {
                    prepare()
                }
            }

            val cachedVideo = CachedVideo(
                localPath = localPath,
                mediaPlayer = mediaPlayer,
                isReady = true
            )

            // Add to memory cache (this will evict LRU if needed)
            memoryCache.put(artworkId, cachedVideo)

            cachedVideo

        } catch (e: Exception) {
            Log.e("ARVideoCache", "Error preparing MediaPlayer for $artworkId: ${e.message}", e)
            null
        }
    }

    private fun getLocalFile(videoUrl: URL): File {
        val urlHash = MD5.from(videoUrl.toString())
        return File(cacheDirectory, "$urlHash.mp4")
    }

    /**
     * Ensure disk cache doesn't exceed maximum size by removing oldest files
     */
    private suspend fun ensureDiskCacheSize() {
        withContext(Dispatchers.IO) {
            val files = cacheDirectory.listFiles() ?: return@withContext

            val totalSize = files.sumOf { it.length() }

            if (totalSize > maxDiskCacheSize) {

                // Sort files by last modified time (oldest first)
                val sortedFiles = files.sortedBy { it.lastModified() }

                val currentSize = totalSize

                for (file in sortedFiles) {
                    if (currentSize <= maxDiskCacheSize * 0.8) {
                        break
                    }

                    file.delete()
                }
            }
        }
    }

    /**
     * Clear memory cache and release MediaPlayer resources
     */
    fun clearMemoryCache() {
        // Release all MediaPlayer instances
        // Take a snapshot of the cache to avoid concurrent modification
        val snapshot = memoryCache.snapshot()
        snapshot.values.forEach { cachedVideo ->
            cachedVideo.mediaPlayer?.release()
        }

        memoryCache.evictAll()
    }

    fun cancelDownloads() {
        downloadJobs.values.forEach { job ->
            job.cancel()
        }
        downloadJobs.clear()
    }

    fun cleanup() {
        cancelDownloads()
        clearMemoryCache()
    }
}
