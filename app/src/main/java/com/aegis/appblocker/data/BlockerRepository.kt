package com.aegis.appblocker.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

/**
 * Single source of truth for the app. Wraps the DAO and adds the schedule-evaluation logic
 * that both the UI and the services rely on.
 */
class BlockerRepository private constructor(context: Context) {

    private val dao = AppDatabase.get(context).dao()

    val targets: Flow<List<BlockedTarget>> = dao.observeTargets()
    val schedules: Flow<List<Schedule>> = dao.observeSchedules()
    val events: Flow<List<BlockEvent>> = dao.observeEvents()

    suspend fun upsertTarget(t: BlockedTarget) = dao.upsertTarget(t)
    suspend fun deleteTarget(t: BlockedTarget) { dao.unlinkTarget(t.id); dao.deleteTarget(t) }
    suspend fun upsertSchedule(s: Schedule) = dao.upsertSchedule(s)
    suspend fun deleteSchedule(s: Schedule) = dao.deleteSchedule(s)

    suspend fun setScheduleTargets(scheduleId: Long, targetIds: List<Long>) {
        dao.clearScheduleLinks(scheduleId)
        targetIds.forEach { dao.linkTarget(ScheduleTarget(scheduleId, it)) }
    }

    suspend fun targetsForSchedule(scheduleId: Long) = dao.targetsForSchedule(scheduleId)
    suspend fun logEvent(e: BlockEvent) = dao.insertEvent(e)
    suspend fun clearEvents() = dao.clearEvents()

    /**
     * Returns the set of package names that should be blocked RIGHT NOW,
     * evaluating both "always block" flags and active schedules.
     */
    suspend fun currentlyBlockedPackages(now: Calendar = Calendar.getInstance()): Set<String> =
        currentlyBlockedTargets(now).mapNotNull { it.packageName }.toSet()

    /** Domains that should be blocked right now (for the VPN filter). */
    suspend fun currentlyBlockedDomains(now: Calendar = Calendar.getInstance()): Set<String> =
        currentlyBlockedTargets(now).mapNotNull { it.domain }.toSet()

    suspend fun currentlyBlockedTargets(now: Calendar = Calendar.getInstance()): List<BlockedTarget> {
        val targets = dao.getTargets().filter { it.enabled }
        val byId = targets.associateBy { it.id }
        val result = LinkedHashSet<BlockedTarget>()

        // Always-blocked targets
        targets.filter { it.alwaysBlock }.forEach { result.add(it) }

        // Schedule-driven targets
        val activeSchedules = dao.getSchedules().filter { it.enabled && isActive(it, now) }
        val links = dao.getLinks()
        for (schedule in activeSchedules) {
            links.filter { it.scheduleId == schedule.id }
                .mapNotNull { byId[it.targetId] }
                .forEach { result.add(it) }
        }
        return result.toList()
    }

    /** Is a schedule active at the given time? Handles windows that cross midnight. */
    fun isActive(s: Schedule, now: Calendar): Boolean {
        val minuteOfDay = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        // Calendar.MONDAY=2 ... SUNDAY=1 -> convert to bit 0..6 (Mon..Sun)
        val dow = when (now.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 0; Calendar.TUESDAY -> 1; Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3; Calendar.FRIDAY -> 4; Calendar.SATURDAY -> 5
            else -> 6
        }
        val dayMatches = (s.daysMask shr dow) and 1 == 1
        val prevDayMatches = (s.daysMask shr ((dow + 6) % 7)) and 1 == 1

        return if (s.startMinuteOfDay <= s.endMinuteOfDay) {
            dayMatches && minuteOfDay in s.startMinuteOfDay until s.endMinuteOfDay
        } else {
            // Crosses midnight
            (dayMatches && minuteOfDay >= s.startMinuteOfDay) ||
                (prevDayMatches && minuteOfDay < s.endMinuteOfDay)
        }
    }

    /** Next time (epoch millis) any schedule flips state, for alarm scheduling. */
    suspend fun nextTransitionMillis(from: Calendar = Calendar.getInstance()): Long? {
        val schedules = dao.getSchedules().filter { it.enabled }
        if (schedules.isEmpty()) return null
        var best: Long? = null
        val probe = from.clone() as Calendar
        probe.set(Calendar.SECOND, 0); probe.set(Calendar.MILLISECOND, 0)
        // Scan the next 24h in 1-minute steps for the first state change per schedule set.
        val initial = schedules.map { isActive(it, probe) }
        for (i in 1..(24 * 60)) {
            probe.add(Calendar.MINUTE, 1)
            val states = schedules.map { isActive(it, probe) }
            if (states != initial) {
                best = probe.timeInMillis
                break
            }
        }
        return best
    }

    companion object {
        @Volatile private var INSTANCE: BlockerRepository? = null
        fun get(context: Context): BlockerRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: BlockerRepository(context).also { INSTANCE = it }
            }
    }
}
