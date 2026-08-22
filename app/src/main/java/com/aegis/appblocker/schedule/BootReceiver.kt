package com.aegis.appblocker.schedule

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Re-arms schedule alarms after the device reboots. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            ScheduleManager.rearm(context)
        }
    }
}
