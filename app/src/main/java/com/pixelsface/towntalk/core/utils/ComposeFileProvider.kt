package com.pixelsface.towntalk.core.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.pixelsface.towntalk.R
import java.io.File

object ComposeFileProvider {
    fun getImageUri(context: Context): Uri {
        val directory = File(context.cacheDir, "images")
        directory.mkdirs()
        val file = File.createTempFile(
            "selected_image_",
            ".jpg",
            directory,
        )
        val authority = context.packageName + ".provider"
        return FileProvider.getUriForFile(
            context,
            authority,
            file,
        )
    }
} 