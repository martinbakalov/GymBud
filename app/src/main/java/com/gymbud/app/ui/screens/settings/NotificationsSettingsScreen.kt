package com.gymbud.app.ui.screens.settings

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.gymbud.app.GymBudApplication
import com.gymbud.app.R
import com.gymbud.app.notifications.DailyNotificationScheduler
import com.gymbud.app.notifications.DailyNotificationWorker
import com.gymbud.app.notifications.NotificationHelper.cancelWorkoutInProgress
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsSettingsScreen(
    onExit: () -> Unit
) {
    val app = LocalContext.current.applicationContext as GymBudApplication
    val prefs = app.preferences
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    val enabled by prefs.dailyNotificationsEnabled.collectAsStateWithLifecycle(initialValue = false)
    val hour by prefs.dailyNotificationHour.collectAsStateWithLifecycle(initialValue = 8)
    val minute by prefs.dailyNotificationMinute.collectAsStateWithLifecycle(initialValue = 0)
    val message by prefs.dailyNotificationMessage.collectAsStateWithLifecycle(initialValue = "")

    var messageDraft by remember(message) { mutableStateOf(message) }

    val timePickerState = rememberTimePickerState(
        initialHour = hour,
        initialMinute = minute,
        is24Hour = true
    )

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_notifications_title)) },
                navigationIcon = {
                    IconButton(onClick = onExit) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            val workoutNotifEnabled by prefs.workoutNotificationsEnabled
                .collectAsStateWithLifecycle(initialValue = true)

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_workout_notif_enabled),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = stringResource(R.string.settings_workout_notif_enabled_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = workoutNotifEnabled,
                    onCheckedChange = { checked ->
                        scope.launch {
                            prefs.setWorkoutNotificationsEnabled(checked)
                            if (!checked) {
                                cancelWorkoutInProgress(context)
                            }
                        }
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_daily_enabled),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = stringResource(R.string.settings_daily_enabled_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = { checked ->
                        scope.launch {
                            prefs.setDailyNotificationsEnabled(checked)
                            DailyNotificationScheduler.scheduleNext(context)
                        }
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = stringResource(R.string.settings_daily_time),
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
            Spacer(Modifier.height(8.dp))

            LaunchedEffect(hour, minute) {
                timePickerState.hour = hour
                timePickerState.minute = minute
            }

            var showTimeDialog by remember { mutableStateOf(false) }

            OutlinedButton(
                onClick = { showTimeDialog = true },
                enabled = enabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "%02d:%02d".format(hour, minute),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (showTimeDialog) {
                TimePickerDialog(
                    timePickerState = timePickerState,
                    onConfirm = {
                        scope.launch {
                            prefs.setDailyNotificationTime(
                                hour = timePickerState.hour,
                                minute = timePickerState.minute
                            )
                            DailyNotificationScheduler.scheduleNext(context)
                        }
                        showTimeDialog = false
                    },
                    onDismiss = { showTimeDialog = false }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text(
                text = stringResource(R.string.settings_daily_message),
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.settings_daily_message_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = messageDraft,
                onValueChange = { messageDraft = it },
                placeholder = {
                    Text(stringResource(R.string.daily_default_message))
                },
                minLines = 2,
                maxLines = 4,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    scope.launch {
                        prefs.setDailyNotificationMessage(messageDraft.trim())
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled && messageDraft.trim() != message
            ) {
                Text(stringResource(R.string.settings_daily_save_message))
            }
            Spacer(Modifier.height(24.dp))

            HorizontalDivider()

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    val request = OneTimeWorkRequestBuilder<DailyNotificationWorker>().build()
                    WorkManager.getInstance(context).enqueue(request)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled
            ) {
                Text(stringResource(R.string.settings_daily_test))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    timePickerState: androidx.compose.material3.TimePickerState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_daily_time)) },
        text = {
            TimeInput(state = timePickerState)
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}