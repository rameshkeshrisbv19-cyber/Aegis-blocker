package com.aegis.appblocker.schedule

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aegis.appblocker.data.BlockEvent
import com.aegis.appblocker.data.BlockerRepository
import com.aegis.appblocker.util.Notifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Fires when a schedule window opens or closes. Diffs the previously-blocked set against the
 * now-blocked set and posts "Blocked"/"Unblocked" notifications accordingly.
 */
class ScheduleAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repo = BlockerRepository.get(context)
                val nowBlocked = repo.currentlyBlockedTargets()
                val prev = LastState.load(context)
                val nowIds = nowBlocked.map { it.id }.toSet()

                // Newly blocked
                nowBlocked.filter { it.id !in prev }.forEach {
                    Notifications.notifyBlocked(context, it.label)
                    repo.logEvent(BlockEvent(label = it.label, packageName = it.packageName, blocked = true))
                }
                // Newly unblocked
                prev.filter { it !in nowIds }.forEach { id ->
                    val label = LastState.labelFor(context, id) ?: "An app"
                    Notifications.notifyUnblocked(context, label)
                    repo.logEvent(BlockEvent(label = label, blocked = false))
                }

                LastState.save(context, nowBlocked.associate { it.id to it.label })
            } finally {
                ScheduleManager.rearm(context)
                pending.finish()
            }
        }
    }
}
