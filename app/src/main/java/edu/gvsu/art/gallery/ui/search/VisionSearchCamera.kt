package edu.gvsu.art.gallery.ui.search

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlin.math.min
import androidx.core.graphics.scale

@Composable
fun VisionSearchCamera(
    onImageCaptured: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val density = LocalDensity.current
    
    val viewFinderSize = with(density) { 250.dp.toPx() }
    val viewFinderBorderSize = with(density) { 3.dp.toPx() }
    val viewFinderBorderRadius = with(density) { 12.dp.toPx() }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { viewContext ->
                val previewView = PreviewView(viewContext)
                val executor = ContextCompat.getMainExecutor(viewContext)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build()

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture
                        )
                    } catch (exception: Exception) {
                        // Handle camera binding exception
                    }
                }, executor)
                previewView
            },
        )

        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val canvasSize = size
            val centerX = canvasSize.width / 2
            val centerY = canvasSize.height / 2
            val viewFinderRadius = min(viewFinderSize / 2, min(centerX, centerY) * 0.8f)

            val viewFinderPath = Path().apply {
                addRoundRect(
                    RoundRect(
                        Rect(
                            Offset(centerX - viewFinderRadius, centerY - viewFinderRadius),
                            Offset(centerX + viewFinderRadius, centerY + viewFinderRadius)
                        ),
                        CornerRadius(viewFinderBorderRadius)
                    )
                )
            }

            clipPath(viewFinderPath, clipOp = ClipOp.Difference) {
                drawRect(SolidColor(Color.Black.copy(alpha = 0.6f)))
            }

            drawPath(
                path = viewFinderPath,
                color = Color.White,
                style = Stroke(viewFinderBorderSize)
            )
        }

        FloatingActionButton(
            onClick = {
                captureImage(
                    imageCapture,
                    context,
                    viewFinderSize.toInt()
                ) { uri ->
                    onImageCaptured(uri)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-32).dp)
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Capture photo",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

private fun captureImage(
    imageCapture: ImageCapture,
    context: Context,
    viewFinderSize: Int,
    onImageCaptured: (Uri) -> Unit
) {
    val outputFileOptions = ImageCapture.OutputFileOptions.Builder(
        context.cacheDir.resolve("temp_camera_image.jpg")
    ).build()

    imageCapture.takePicture(
        outputFileOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val imageFile = context.cacheDir.resolve("temp_camera_image.jpg")
                
                // Load the saved image as bitmap
                val bitmap = android.graphics.BitmapFactory.decodeFile(imageFile.absolutePath)
                
                // Apply rotation correction based on EXIF data
                val rotatedBitmap = applyExifRotation(bitmap, imageFile.absolutePath)
                
                val croppedBitmap = cropImageToViewFinder(
                    rotatedBitmap,
                    viewFinderSize,
                )
                
                // Save the cropped bitmap back to file
                val croppedFile = context.cacheDir.resolve("temp_camera_image_cropped.jpg")
                croppedFile.outputStream().use { outputStream ->
                    croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                }
                
                val uri = Uri.fromFile(croppedFile)
                onImageCaptured(uri)
            }

            override fun onError(exception: ImageCaptureException) {
                // Handle error
            }
        }
    )
}

private fun cropImageToViewFinder(
    bitmap: Bitmap,
    viewFinderSize: Int,
): Bitmap {
    val size = min(bitmap.width, bitmap.height)
    val x = (bitmap.width - size) / 2
    val y = (bitmap.height - size) / 2
    
    val squareBitmap = Bitmap.createBitmap(bitmap, x, y, size, size)
    
    val scaledBitmap = squareBitmap.scale(viewFinderSize, viewFinderSize)

    return scaledBitmap
}

private fun applyExifRotation(bitmap: Bitmap, imagePath: String): Bitmap {
    val exif = ExifInterface(imagePath)
    val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    
    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
        else -> return bitmap // No rotation needed
    }
    
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}