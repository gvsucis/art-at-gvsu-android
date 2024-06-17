package edu.gvsu.art.gallery.lib

import android.content.Context
import android.net.Uri
import androidx.annotation.FloatRange
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import okio.ForwardingSource
import okio.Source
import okio.buffer
import okio.sink
import java.io.File
import kotlin.coroutines.coroutineContext

object FileDownloader {
    suspend fun download(
        url: String,
        directory: File,
        onProgressUpdate: (progress: Float) -> Unit = {}
    ): Result<File> {
        val fileName = MD5.from(url)
        val outputFile = File(directory, fileName)

        if (!directory.exists()) {
            directory.mkdir()
        }

        if (outputFile.exists()) {
            return Result.success(outputFile)
        }

        val request: Request = Request.Builder()
            .url(url)
            .build()

        val progressListener: ProgressListener = object : ProgressListener {
            override fun update(progress: Float) {
                onProgressUpdate(progress)
            }
        }

        val client = OkHttpClient.Builder()
            .addNetworkInterceptor(Interceptor { chain: Interceptor.Chain ->
                val originalResponse = chain.proceed(chain.request())
                originalResponse.newBuilder()
                    .body(ProgressResponseBody(originalResponse.body!!, progressListener))
                    .build()
            })
            .build()


        return try {
            val response = client.newCall(request).await()

            val fileSink = outputFile.sink().buffer()
            fileSink.writeAll(response.body!!.source())
            withContext(coroutineContext) {
                fileSink.close()
            }

            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private class ProgressResponseBody(
        private val responseBody: ResponseBody,
        private val progressListener: ProgressListener
    ) : ResponseBody() {
        private var bufferedSource: BufferedSource? = null

        override fun contentType(): MediaType? {
            return responseBody.contentType()
        }

        override fun contentLength(): Long {
            return responseBody.contentLength()
        }

        override fun source(): BufferedSource {
            if (bufferedSource == null) {
                bufferedSource = source(responseBody.source()).buffer()
            }
            return bufferedSource!!
        }

        private fun source(source: Source): Source {
            return object : ForwardingSource(source) {
                var totalBytesRead: Long = 0L

                override fun read(sink: Buffer, byteCount: Long): Long {
                    val bytesRead = super.read(sink, byteCount)

                    // read() returns the number of bytes read, or -1 if this source is exhausted.
                    totalBytesRead += if (bytesRead != -1L) bytesRead else 0

                    if (bytesRead == -1L) {
                        progressListener.update(1f)
                    } else {
                        val runningTotal = (totalBytesRead / responseBody.contentLength().toFloat())

                        progressListener.update(runningTotal)
                    }

                    return bytesRead
                }
            }
        }
    }

    internal interface ProgressListener {
        fun update(@FloatRange(from = 0.0, to = 1.0) progress: Float)
    }
}
