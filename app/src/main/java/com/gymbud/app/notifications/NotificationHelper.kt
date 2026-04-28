package com.gymbud.app.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.gymbud.app.MainActivity
import com.gymbud.app.R

object NotificationHelper {

    const val CHANNEL_WORKOUT_IN_PROGRESS = "workout_in_progress"
    const val CHANNEL_DAILY_MOTIVATION = "daily_motivation"

    const val NOTIFICATION_ID_WORKOUT_IN_PROGRESS = 1001
    const val NOTIFICATION_ID_DAILY_MOTIVATION = 1002

    fun createChannels(context: Context) {

        val manager = ContextCompat.getSystemService(context, NotificationManager::class.java)
            ?: return

        val workoutChannel = NotificationChannel(
            CHANNEL_WORKOUT_IN_PROGRESS,
            context.getString(R.string.notif_channel_workout_title),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.notif_channel_workout_desc)
            setShowBadge(false)
            enableVibration(false)
        }

        val dailyChannel = NotificationChannel(
            CHANNEL_DAILY_MOTIVATION,
            context.getString(R.string.notif_channel_daily_title),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notif_channel_daily_desc)
            setShowBadge(true)
        }

        manager.createNotificationChannel(workoutChannel)
        manager.createNotificationChannel(dailyChannel)
    }
    @SuppressLint("MissingPermission")
    fun showWorkoutInProgress(
        context: Context,
        workoutName: String,
        elapsedText: String
    ) {

        if (!hasNotificationPermission(context)) return

        val tapIntent = Intent(context, MainActivity::class.java).apply {

            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_WORKOUT_IN_PROGRESS)
            .setSmallIcon(R.drawable.ic_workout_notification)
            .setContentTitle(workoutName.ifBlank { context.getString(R.string.workout_empty_name) })
            .setContentText(elapsedText)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setShowWhen(false)
            .build()

        NotificationManagerCompat.from(context).notify(
            NOTIFICATION_ID_WORKOUT_IN_PROGRESS,
            notification
        )
    }

    fun cancelWorkoutInProgress(context: Context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID_WORKOUT_IN_PROGRESS)
    }

    private fun hasNotificationPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }
}