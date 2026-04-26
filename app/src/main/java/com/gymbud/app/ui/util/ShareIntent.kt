package com.gymbud.app.ui.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

fun shareWorkout(
    context: Context,
    summary: String,
    photoPath: String?
) {
    val photoUri: android.net.Uri? = photoPath
        ?.let { File(it) }
        ?.takeIf { it.exists() }
        ?.let {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                it
            )
        }

    val intent = Intent(Intent.ACTION_SEND).apply {
        if (photoUri != null) {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, photoUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            type = "text/plain"
        }
        putExtra(Intent.EXTRA_TEXT, summary)
    }

    context.startActivity(Intent.createChooser(intent, null))
}