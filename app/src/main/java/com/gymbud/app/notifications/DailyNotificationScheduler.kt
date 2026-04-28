package com.gymbud.app.notifications

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.gymbud.app.GymBudApplication
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Calendar
import java.util.concurrent.TimeUnit

object DailyNotificationScheduler {

    private const val WORK_NAME = "daily_motivation_notification"

    fun scheduleNext(context: Context) {
        val app = context.applicationContext as GymBudApplication
        val prefs = app.preferences
        val workManager = WorkManager.getInstance(context)

        val (enabled, hour, minute) = runBlocking {
            Triple(
                prefs.dailyNotificationsEnabled.first(),
                prefs.dailyNotificationHour.first(),
                prefs.dailyNotificationMinute.first()
            )
        }

        if (!enabled) {
            workManager.cancelUniqueWork(WORK_NAME)
            return
        }

        val delayMillis = computeDelayUntil(hour, minute)

        val request = OneTimeWorkRequestBuilder<DailyNotificationWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    private fun computeDelayUntil(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (target.timeInMillis <= now.timeInMillis) {
            target.add(Calendar.DAY_OF_MONTH, 1)
        }
        return target.timeInMillis - now.timeInMillis
    }
}