package com.gymbud.app.ui.util

import android.content.Context
import androidx.core.content.FileProvider
import android.net.Uri
import java.io.File

fun createWorkoutPhotoFile(context: Context): Pair<File, Uri> {
    val picturesDir = File(context.filesDir, "Pictures").apply { mkdirs() }
    val file = File(picturesDir, "workout_${System.currentTimeMillis()}.jpg")
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
    return file to uri
}
fun copyUriToWorkoutPhoto(context: Context, source: Uri): File? {
    val picturesDir = File(context.filesDir, "Pictures").apply { mkdirs() }
    val dest = File(picturesDir, "workout_${System.currentTimeMillis()}.jpg")
    return try {
        context.contentResolver.openInputStream(source)?.use { input ->
            dest.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        dest
    } catch (e: Exception) {
        dest.delete()
        null
    }
}