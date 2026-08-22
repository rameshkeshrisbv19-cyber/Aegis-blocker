package com.aegis.appblocker.ui

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimeUtils {
    private val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    fun minuteToText(min: Int): String {
        val h = min / 60
        val m = min % 60
        val am = h < 12
        val h12 = when {
            h == 0 -> 12; h > 12 -> h - 12; else -> h
        }
        return "%d:%02d %s".format(h12, m, if (am) "AM" else "PM")
    }

    fun daysMaskToText(mask: Int): String {
        if (mask == 0b1111111) return "Every day"
        if (mask == 0b0011111) return "Weekdays"
        if (mask == 0b1100000) return "Weekends"
        val parts = dayLabels.filterIndexed { i, _ -> (mask shr i) and 1 == 1 }
        return if (parts.isEmpty()) "No days" else parts.joinToString(" ")
    }

    fun dayLabel(index: Int) = dayLabels[index]

    fun eventTime(ts: Long): String =
        SimpleDateFormat("MMM d · h:mm a", Locale.getDefault()).format(Date(ts))
}
