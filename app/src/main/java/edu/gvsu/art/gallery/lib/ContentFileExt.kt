package edu.gvsu.art.gallery.lib

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

fun Context.fileURI(file: File): Uri =
    FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)

fun Context.arAssetsDirectory(): File = File(cacheDir, "ar_assets")

// Reference images seed ARCore's image database once; kept apart from the bounded
// AR media cache so they never evict (or get evicted by) replayable videos/models.
fun Context.arReferencesDirectory(): File = File(cacheDir, "ar_references")

fun Context.searchImagesDirectory(): File = File(cacheDir, "vision_search")

fun Context.createTempImage(): Uri {
    val fileName = "image_${System.currentTimeMillis()}"

    val directory = searchImagesDirectory().apply {
        if (!exists()) {
            mkdir()
        }
    }

    val tempFile = File.createTempFile(fileName, ".jpg", directory).apply {
        createNewFile()
    }

    return fileURI(tempFile)
}
