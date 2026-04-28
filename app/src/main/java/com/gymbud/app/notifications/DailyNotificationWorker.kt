package com.gymbud.app.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gymbud.app.GymBudApplication
import com.gymbud.app.MainActivity
import com.gymbud.app.R
import kotlinx.coroutines.flow.first

class DailyNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    @SuppressLint("MissingPermission")
    override suspend fun doWork(): Result {
        val app = applicationContext as GymBudApplication
        val prefs = app.preferences

        val enabled = prefs.dailyNotificationsEnabled.first()
        if (!enabled) return Result.success()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return Result.success()
        }

        val userMessage = prefs.dailyNotificationMessage.first()
        val message = userMessage.ifBlank {
            applicationContext.getString(R.string.daily_default_message)
        }

        val tapIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            1,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            applicationContext,
            NotificationHelper.CHANNEL_DAILY_MOTIVATION
        )
            .setSmallIcon(R.drawable.ic_workout_notification)
            .setContentTitle(applicationContext.getString(R.string.app_name))
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(
            NotificationHelper.NOTIFICATION_ID_DAILY_MOTIVATION,
            notification
        )

        DailyNotificationScheduler.scheduleNext(applicationContext)

        return Result.success()
    }
}