package com.aegis.appblocker.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.aegis.appblocker.MainActivity
import com.aegis.appblocker.R

object Notifications {
    const val CHANNEL_EVENTS = "aegis_events"
    const val CHANNEL_SERVICE = "aegis_service"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_EVENTS, "Block & Unblock Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Notifies you when an app or site is blocked or unblocked." }
        )
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_SERVICE, "Protection Running",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Shows that Aegis protection is active." }
        )
    }

    private fun hasPermission(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ActivityCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

    fun notifyBlocked(context: Context, label: String) {
        showEvent(context, "🛡️ Blocked $label", "$label is blocked right now to protect your focus.")
    }

    fun notifyUnblocked(context: Context, label: String) {
        showEvent(context, "✅ Unblocked $label", "$label is available again. Use it mindfully!")
    }

    private fun showEvent(context: Context, title: String, text: String) {
        if (!hasPermission(context)) return
        val tap = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val n = NotificationCompat.Builder(context, CHANNEL_EVENTS)
            .setSmallIcon(R.drawable.ic_shield)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(tap)
            .build()
        NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), n)
    }
}
