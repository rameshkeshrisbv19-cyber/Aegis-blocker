package com.aegis.appblocker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A blocked target. Either an installed app (packageName set) OR a website (domain set).
 */
@Entity(tableName = "blocked_targets")
data class BlockedTarget(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,             // Display name (app name or site name)
    val packageName: String? = null, // e.g. "com.instagram.android"
    val domain: String? = null,      // e.g. "instagram.com"
    val isApp: Boolean = true,
    /** If true this target is blocked at all times, ignoring schedules. */
    val alwaysBlock: Boolean = false,
    val enabled: Boolean = true
)

/**
 * A schedule window during which linked targets are blocked.
 * daysMask: bit 0 = Monday ... bit 6 = Sunday.
 */
@Entity(tableName = "schedules")
data class Schedule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val startMinuteOfDay: Int,  // 0..1439
    val endMinuteOfDay: Int,    // 0..1439
    val daysMask: Int,          // bitmask of weekdays
    val enabled: Boolean = true,
    val accentColor: Long = 0xFF6C63FF
)

/** Join: which targets belong to which schedule. */
@Entity(tableName = "schedule_targets", primaryKeys = ["scheduleId", "targetId"])
data class ScheduleTarget(
    val scheduleId: Long,
    val targetId: Long
)

/** History log of block/allow events for the UI activity feed. */
@Entity(tableName = "events")
data class BlockEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val packageName: String? = null,
    val blocked: Boolean,       // true = blocked, false = allowed/unblocked
    val timestamp: Long = System.currentTimeMillis()
)
