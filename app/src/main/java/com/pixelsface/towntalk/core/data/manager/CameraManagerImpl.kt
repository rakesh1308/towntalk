package com.pixelsface.towntalk.core.data.manager

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.pixelsface.towntalk.core.domain.manager.CameraManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CameraManagerImpl @Inject constructor(
    private val context: Context
) : CameraManager {

    override fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun requestCameraPermission() {
        // This is a placeholder. The actual permission request should be handled by the UI layer
        // using ActivityResultLauncher
    }

    override fun hasCamera(): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }

    override fun getMaxImageSize(): Long {
        return MAX_IMAGE_SIZE
    }

    override suspend fun takePhoto(): Flow<Uri> = flow {
        withContext(Dispatchers.IO) {
            try {
                val photoFile = createImageFile()
                val photoUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    photoFile
                )
                emit(photoUri)
            } catch (e: Exception) {
                throw IllegalStateException("Failed to create image file", e)
            }
        }
    }

    override suspend fun compressImage(uri: Uri): Uri = withContext(Dispatchers.IO) {
        try {
            // Read the image
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Calculate new dimensions while maintaining aspect ratio
            val maxDimension = 1024 // Max width or height
            val ratio = minOf(
                maxDimension.toFloat() / originalBitmap.width,
                maxDimension.toFloat() / originalBitmap.height
            )
            val newWidth = (originalBitmap.width * ratio).toInt()
            val newHeight = (originalBitmap.height * ratio).toInt()

            // Create compressed bitmap
            val compressedBitmap = Bitmap.createScaledBitmap(
                originalBitmap,
                newWidth,
                newHeight,
                true
            )

            // Save the compressed image
            val compressedFile = createImageFile()
            FileOutputStream(compressedFile).use { out ->
                compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
            }

            // Clean up
            originalBitmap.recycle()
            compressedBitmap.recycle()

            // Return URI for the compressed image
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                compressedFile
            )
        } catch (e: Exception) {
            throw IllegalStateException("Failed to compress image", e)
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    companion object {
        private const val MAX_IMAGE_SIZE = 10L * 1024 * 1024 // 10MB
    }
} 