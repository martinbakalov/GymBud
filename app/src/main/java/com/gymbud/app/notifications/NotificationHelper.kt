package com.gymbud.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.content.ContextCompat
import com.gymbud.app.R

object NotificationHelper {

    const val CHANNEL_WORKOUT_IN_PROGRESS = "workout_in_progress"
    const val CHANNEL_DAILY_MOTIVATION = "daily_motivation"

    const val NOTIFICATION_ID_WORKOUT_IN_PROGRESS = 1001
    const val NOTIFICATION_ID_DAILY_MOTIVATION = 1002

    fun createChannels(context: Context) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

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
}