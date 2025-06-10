package com.pixelsface.towntalk.core.domain.manager

import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface CameraManager {
    /**
     * Checks if the app has camera permission
     */
    fun hasCameraPermission(): Boolean

    /**
     * Requests camera permission from the user
     */
    fun requestCameraPermission()

    /**
     * Takes a photo using the device camera
     * @return Flow emitting the URI of the captured image
     */
    suspend fun takePhoto(): Flow<Uri>

    /**
     * Checks if the device has a camera
     */
    fun hasCamera(): Boolean

    /**
     * Gets the maximum allowed image size in bytes
     */
    fun getMaxImageSize(): Long

    /**
     * Compresses an image to meet size requirements
     * @param uri URI of the image to compress
     * @return URI of the compressed image
     */
    suspend fun compressImage(uri: Uri): Uri
} 