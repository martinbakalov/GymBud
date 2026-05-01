package com.gymbud.app.ui.screens.workouts

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gymbud.app.R
import com.gymbud.app.domain.model.WeightUnit
import com.gymbud.app.domain.model.WorkoutStats
import com.gymbud.app.ui.util.copyUriToWorkoutPhoto
import com.gymbud.app.ui.util.createWorkoutPhotoFile
import com.gymbud.app.ui.util.formatDurationCompact
import com.gymbud.app.ui.util.formatVolumeNumber
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinishWorkoutDialog(
    initialName: String,
    nameFallback: String,
    initialNotes: String,
    initialPhotoPath: String?,
    durationMillis: Long,
    stats: WorkoutStats,
    unit: WeightUnit,
    onSave: (name: String, notes: String?, photoPath: String?) -> Unit,
    onNameChange: (name: String) -> Unit,
    onPhotoChange: (photoPath: String?) -> Unit,
    onNotesChange: (notes: String?) -> Unit,
    onResume: () -> Unit
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf(initialName) }
    var notes by remember { mutableStateOf(initialNotes) }
    var photoFile by remember {
        mutableStateOf(initialPhotoPath?.let { File(it).takeIf(File::exists) })
    }
    var pendingUri by remember { mutableStateOf<Uri?>(null) }
    var pendingFile by remember { mutableStateOf<File?>(null) }
    val unitSuffix = stringResource(
        if (unit == WeightUnit.KG) R.string.workout_kg else R.string.workout_lbs
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val file = pendingFile
        if (success && file != null) {
            photoFile = file
            onPhotoChange(file.absolutePath)
        } else {
            file?.delete()
        }
        pendingFile = null
        pendingUri = null
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val copied = copyUriToWorkoutPhoto(context, uri)
            if (copied != null) {
                photoFile = copied
                onPhotoChange(copied.absolutePath)
            }
        }
    }

    LaunchedEffect(pendingUri) {
        pendingUri?.let { cameraLauncher.launch(it) }
    }

    AlertDialog(
        onDismissRequest = onResume,
        title = { Text(stringResource(R.string.finish_dialog_title)) },
        text = {
            val focusManager = LocalFocusManager.current
            Column (
                modifier = Modifier.pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        onNameChange(it)
                    },
                    label = { Text(stringResource(R.string.finish_dialog_name_label)) },
                    placeholder = { Text(nameFallback) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatSummary(
                        label = stringResource(R.string.stats_duration),
                        value = formatDurationCompact(durationMillis)
                    )
                    StatSummary(
                        label = stringResource(R.string.stats_sets),
                        value = stats.totalSets.toString()
                    )
                    StatSummary(
                        label = stringResource(R.string.stats_volume),
                        value = "${formatVolumeNumber(stats.totalVolumeKg, unit)} $unitSuffix"
                    )
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = {
                        notes = it
                        onNotesChange(it.ifBlank { null })
                    },
                    label = { Text(stringResource(R.string.finish_dialog_notes_hint)) },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                val photo = photoFile
                if (photo == null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val (file, uri) = createWorkoutPhotoFile(context)
                                pendingFile = file
                                pendingUri = uri
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null)
                            Spacer(Modifier.size(6.dp))
                            Text(stringResource(R.string.take_photo))
                        }
                        OutlinedButton(
                            onClick = {
                                galleryLauncher.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                            Spacer(Modifier.size(6.dp))
                            Text(stringResource(R.string.gallery_photo))
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        coil.compose.AsyncImage(
                            model = photo,
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(8.dp)
                                )
                        )
                        IconButton(
                            onClick = {
                                photo.delete()
                                photoFile = null
                            },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.remove_photo),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    name.trim(),
                    notes.ifBlank { null },
                    photoFile?.absolutePath
                )
            }) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onResume) {
                Text(stringResource(R.string.finish_dialog_resume))
            }
        }
    )
}

@Composable
private fun StatSummary(label: String, value: String) {
    Column {
        Text(text = value, style = MaterialTheme.typography.titleMedium)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}