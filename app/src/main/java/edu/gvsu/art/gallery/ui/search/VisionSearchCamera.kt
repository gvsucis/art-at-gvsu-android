package edu.gvsu.art.gallery.ui.search

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
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
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.scale
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.compose.LocalLifecycleOwner

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
    val containerSize = LocalWindowInfo.current.containerSize
    val viewFinderPadding = with(density) { 16.dp.toPx() }
    val viewFinderSize = containerSize.width.toFloat() - (viewFinderPadding * 2)

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
            val viewFinderRadius = viewFinderSize / 2
            
            // Position viewfinder with padding from top and center horizontally
            val viewFinderCenterY = viewFinderPadding + viewFinderRadius

            val viewFinderPath = Path().apply {
                addRoundRect(
                    RoundRect(
                        Rect(
                            Offset(centerX - viewFinderRadius, viewFinderCenterY - viewFinderRadius),
                            Offset(centerX + viewFinderRadius, viewFinderCenterY + viewFinderRadius)
                        ),
                        CornerRadius(viewFinderBorderRadius)
                    )
                )
            }

            clipPath(viewFinderPath, clipOp = ClipOp.Difference) {
                drawRect(SolidColor(Color.Black))
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
                    viewFinderSize.toInt(),
                    viewFinderPadding,
                    containerSize
                ) { uri ->
                    onImageCaptured(uri)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
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
    viewFinderPadding: Float,
    containerSize: IntSize,
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

                val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)

                val rotatedBitmap = applyExifRotation(bitmap, imageFile.absolutePath)

                // Debug: Log the dimensions
                android.util.Log.d("CameraCrop", "Bitmap: ${rotatedBitmap.width}x${rotatedBitmap.height}")
                android.util.Log.d("CameraCrop", "Container: ${containerSize.width}x${containerSize.height}")
                android.util.Log.d("CameraCrop", "ViewFinder size: $viewFinderSize, padding: $viewFinderPadding")
                
                // Use actual screen dimensions instead of container size
                val displayMetrics = context.resources.displayMetrics
                val croppedBitmap = cropImageToViewFinder(
                    rotatedBitmap,
                    viewFinderSize,
                    viewFinderPadding,
                    displayMetrics.widthPixels,
                    displayMetrics.heightPixels
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
    viewFinderPadding: Float,
    viewportWidth: Int,
    viewportHeight: Int
): Bitmap {
    // Calculate how the preview is scaled and positioned on viewport
    val previewRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
    val viewportRatio = viewportWidth.toFloat() / viewportHeight.toFloat()

    val scale: Float
    val previewWidth: Float
    val previewHeight: Float
    val previewOffsetX: Float
    val previewOffsetY: Float

    if (previewRatio > viewportRatio) {
        // Preview is wider than viewport, scale by height and center horizontally
        scale = viewportHeight.toFloat() / bitmap.height.toFloat()
        previewWidth = bitmap.width * scale
        previewHeight = viewportHeight.toFloat()
        previewOffsetX = (viewportWidth - previewWidth) / 2f
        previewOffsetY = 0f
    } else {
        // Preview is taller than viewport, scale by width and center vertically
        scale = viewportWidth.toFloat() / bitmap.width.toFloat()
        previewWidth = viewportWidth.toFloat()
        previewHeight = bitmap.height * scale
        previewOffsetX = 0f
        previewOffsetY = (viewportHeight - previewHeight) / 2f
    }

    val viewFinderCenterX = viewportWidth / 2f
    val viewFinderRadius = viewFinderSize / 2f
    val viewFinderCenterY = viewFinderPadding + viewFinderRadius

    val previewLeft = viewFinderCenterX - viewFinderRadius - previewOffsetX
    val previewTop = viewFinderCenterY - viewFinderRadius - previewOffsetY
    val previewRight = viewFinderCenterX + viewFinderRadius - previewOffsetX
    val previewBottom = viewFinderCenterY + viewFinderRadius - previewOffsetY

    val cropLeft = (previewLeft / scale).toInt()
    val cropTop = (previewTop / scale).toInt()
    val cropRight = (previewRight / scale).toInt()
    val cropBottom = (previewBottom / scale).toInt()

    val finalCropLeft = maxOf(0, cropLeft)
    val finalCropTop = maxOf(0, cropTop)
    val finalCropRight = minOf(bitmap.width, cropRight)
    val finalCropBottom = minOf(bitmap.height, cropBottom)

    val cropWidth = finalCropRight - finalCropLeft
    val cropHeight = finalCropBottom - finalCropTop

    val croppedBitmap = Bitmap.createBitmap(
        bitmap,
        finalCropLeft,
        finalCropTop,
        cropWidth,
        cropHeight
    )

    val scaledBitmap = croppedBitmap.scale(viewFinderSize, viewFinderSize)

    return scaledBitmap
}

private fun applyExifRotation(bitmap: Bitmap, imagePath: String): Bitmap {
    val exif = ExifInterface(imagePath)
    val orientation =
        exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

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