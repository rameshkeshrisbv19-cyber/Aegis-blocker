package com.aegis.appblocker.ui

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aegis.appblocker.data.*
import com.aegis.appblocker.schedule.ScheduleManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BlockerViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = BlockerRepository.get(app)

    val targets: StateFlow<List<BlockedTarget>> =
        repo.targets.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val schedules: StateFlow<List<Schedule>> =
        repo.schedules.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val events: StateFlow<List<BlockEvent>> =
        repo.events.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val installedApps: StateFlow<List<InstalledApp>> = _installedApps

    fun loadInstalledApps() {
        if (_installedApps.value.isNotEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            val pm = getApplication<Application>().packageManager
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            val apps = pm.queryIntentActivities(intent, 0)
                .mapNotNull { ri ->
                    val pkg = ri.activityInfo.packageName
                    if (pkg == getApplication<Application>().packageName) return@mapNotNull null
                    InstalledApp(
                        packageName = pkg,
                        label = ri.loadLabel(pm).toString(),
                        icon = runCatching { ri.loadIcon(pm) }.getOrNull()
                    )
                }
                .distinctBy { it.packageName }
                .sortedBy { it.label.lowercase() }
            _installedApps.value = apps
        }
    }

    fun addApp(a: InstalledApp, alwaysBlock: Boolean) = viewModelScope.launch {
        repo.upsertTarget(
            BlockedTarget(label = a.label, packageName = a.packageName, isApp = true, alwaysBlock = alwaysBlock)
        )
        ScheduleManager.rearm(getApplication())
    }

    fun addWebsite(domainRaw: String, alwaysBlock: Boolean) = viewModelScope.launch {
        val domain = domainRaw.trim().removePrefix("http://").removePrefix("https://")
            .removePrefix("www.").substringBefore("/").lowercase()
        if (domain.isBlank()) return@launch
        repo.upsertTarget(
            BlockedTarget(label = domain, domain = domain, isApp = false, alwaysBlock = alwaysBlock)
        )
        ScheduleManager.rearm(getApplication())
    }

    fun deleteTarget(t: BlockedTarget) = viewModelScope.launch { repo.deleteTarget(t) }

    fun toggleTargetEnabled(t: BlockedTarget) = viewModelScope.launch {
        repo.upsertTarget(t.copy(enabled = !t.enabled))
        ScheduleManager.rearm(getApplication())
    }

    fun toggleAlwaysBlock(t: BlockedTarget) = viewModelScope.launch {
        repo.upsertTarget(t.copy(alwaysBlock = !t.alwaysBlock))
        ScheduleManager.rearm(getApplication())
    }

    // --- Schedules ---
    suspend fun scheduleTargetIds(scheduleId: Long): List<Long> =
        withContext(Dispatchers.IO) { repo.targetsForSchedule(scheduleId) }

    fun saveSchedule(schedule: Schedule, targetIds: List<Long>) = viewModelScope.launch {
        val id = repo.upsertSchedule(schedule)
        repo.setScheduleTargets(if (schedule.id == 0L) id else schedule.id, targetIds)
        ScheduleManager.rearm(getApplication())
    }

    fun toggleScheduleEnabled(s: Schedule) = viewModelScope.launch {
        repo.upsertSchedule(s.copy(enabled = !s.enabled))
        ScheduleManager.rearm(getApplication())
    }

    fun deleteSchedule(s: Schedule) = viewModelScope.launch { repo.deleteSchedule(s) }

    fun clearEvents() = viewModelScope.launch { repo.clearEvents() }
}
