package edu.gvsu.art.gallery.lib

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

fun Context.fileURI(file: File): Uri =
    FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)

fun Context.arAssetsDirectory(): File = File(cacheDir, "ar_assets")
