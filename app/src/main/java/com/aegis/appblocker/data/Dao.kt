package com.aegis.appblocker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockerDao {

    // --- Targets ---
    @Query("SELECT * FROM blocked_targets ORDER BY label COLLATE NOCASE")
    fun observeTargets(): Flow<List<BlockedTarget>>

    @Query("SELECT * FROM blocked_targets")
    suspend fun getTargets(): List<BlockedTarget>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTarget(target: BlockedTarget): Long

    @Delete
    suspend fun deleteTarget(target: BlockedTarget)

    @Query("DELETE FROM schedule_targets WHERE targetId = :targetId")
    suspend fun unlinkTarget(targetId: Long)

    // --- Schedules ---
    @Query("SELECT * FROM schedules ORDER BY startMinuteOfDay")
    fun observeSchedules(): Flow<List<Schedule>>

    @Query("SELECT * FROM schedules")
    suspend fun getSchedules(): List<Schedule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSchedule(schedule: Schedule): Long

    @Delete
    suspend fun deleteSchedule(schedule: Schedule)

    // --- Links ---
    @Query("SELECT * FROM schedule_targets")
    suspend fun getLinks(): List<ScheduleTarget>

    @Query("SELECT targetId FROM schedule_targets WHERE scheduleId = :scheduleId")
    suspend fun targetsForSchedule(scheduleId: Long): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun linkTarget(link: ScheduleTarget)

    @Query("DELETE FROM schedule_targets WHERE scheduleId = :scheduleId")
    suspend fun clearScheduleLinks(scheduleId: Long)

    // --- Events ---
    @Query("SELECT * FROM events ORDER BY timestamp DESC LIMIT 100")
    fun observeEvents(): Flow<List<BlockEvent>>

    @Insert
    suspend fun insertEvent(event: BlockEvent)

    @Query("DELETE FROM events")
    suspend fun clearEvents()
}
