package com.gymbud.app.ui.util

import java.text.DateFormat
import java.util.Date

fun formatDurationCompact(millis: Long?): String {
    if (millis == null || millis <= 0L) return "—"
    val totalMinutes = millis / 1000 / 60
    val h = totalMinutes / 60
    val m = totalMinutes % 60
    return if (h > 0) "${h}h ${"%02d".format(m)}m" else "${m}m"
}

fun formatDurationTicking(millis: Long): String {
    val totalSeconds = millis / 1000
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s)
    else "%d:%02d".format(m, s)
}

fun formatSessionDateTime(millis: Long?): String {
    if (millis == null) return ""
    val date = Date(millis)
    val dateStr = DateFormat.getDateInstance(DateFormat.MEDIUM).format(date)
    val timeStr = DateFormat.getTimeInstance(DateFormat.SHORT).format(date)
    return "$dateStr · $timeStr"
}