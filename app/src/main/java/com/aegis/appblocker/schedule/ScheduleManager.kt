package com.aegis.appblocker.schedule

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.aegis.appblocker.data.BlockerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Schedules an exact alarm for the next moment any schedule flips state. When the alarm fires,
 * ScheduleAlarmReceiver posts the right block/unblock notifications and re-arms the next alarm.
 */
object ScheduleManager {

    private const val REQUEST_CODE = 7001

    fun rearm(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val repo = BlockerRepository.get(context)
            val next = repo.nextTransitionMillis() ?: return@launch
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pi = PendingIntent.getBroadcast(
                context, REQUEST_CODE,
                Intent(context, ScheduleAlarmReceiver::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next, pi)
                } else {
                    am.setExact(AlarmManager.RTC_WAKEUP, next, pi)
                }
            } catch (e: SecurityException) {
                am.set(AlarmManager.RTC_WAKEUP, next, pi)
            }
        }
    }
}
