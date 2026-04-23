package com.gymbud.app.ui.screens.workouts

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gymbud.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutBlockerDialog(
    onResume: () -> Unit,
    onDiscardAndStart: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(stringResource(R.string.blocker_title)) },
        text = { Text(stringResource(R.string.blocker_body)) },
        confirmButton = {
            TextButton(onClick = onResume) {
                Text(stringResource(R.string.blocker_resume))
            }
        },
        dismissButton = {
            androidx.compose.foundation.layout.Row {
                TextButton(
                    onClick = onDiscardAndStart,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.blocker_discard_and_start))
                }
                TextButton(onClick = onCancel) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        }
    )
}