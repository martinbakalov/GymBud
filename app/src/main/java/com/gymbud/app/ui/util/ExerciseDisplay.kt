package com.gymbud.app.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.gymbud.app.data.local.entity.Exercise

@Composable
fun Exercise.displayName(): String {
    val key = nameKey
    if (key.isNullOrBlank()) return name

    val context = LocalContext.current
    val resourceId = context.resources.getIdentifier(key, "string", context.packageName)
    return if (resourceId != 0) stringResource(resourceId) else name
}