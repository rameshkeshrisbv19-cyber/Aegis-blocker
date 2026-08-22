package com.aegis.appblocker.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.aegis.appblocker.block.BlockScreenActivity
import com.aegis.appblocker.data.BlockEvent
import com.aegis.appblocker.data.BlockerRepository
import com.aegis.appblocker.util.Notifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Watches which app comes to the foreground. When a blocked app appears (per the current
 * schedule/always-block rules), it launches a full-screen block screen over it and posts a
 * notification. This is the no-root method for blocking apps.
 */
class AppBlockAccessibilityService : AccessibilityService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var repo: BlockerRepository

    // Throttle so we don't spam the block screen / notifications for the same app.
    private var lastBlockedPackage: String? = null
    private var lastBlockedAt = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        repo = BlockerRepository.get(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return

        // Ignore ourselves and system UI.
        if (pkg == packageName || pkg == "com.android.systemui") return

        scope.launch {
            val blocked = withContext(Dispatchers.IO) { repo.currentlyBlockedPackages() }
            if (pkg in blocked) {
                handleBlocked(pkg)
            } else if (pkg != lastBlockedPackage) {
                lastBlockedPackage = null
            }
        }
    }

    private suspend fun handleBlocked(pkg: String) {
        val now = System.currentTimeMillis()
        if (pkg == lastBlockedPackage && now - lastBlockedAt < 1200) return
        lastBlockedPackage = pkg
        lastBlockedAt = now

        val label = withContext(Dispatchers.IO) { appLabel(pkg) }

        // Send user "home" first so back doesn't return to the blocked app, then show block screen.
        performGlobalAction(GLOBAL_ACTION_HOME)

        val intent = Intent(this, BlockScreenActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            putExtra(BlockScreenActivity.EXTRA_LABEL, label)
            putExtra(BlockScreenActivity.EXTRA_PACKAGE, pkg)
        }
        startActivity(intent)

        Notifications.notifyBlocked(this, label)
        withContext(Dispatchers.IO) {
            repo.logEvent(BlockEvent(label = label, packageName = pkg, blocked = true))
        }
    }

    private fun appLabel(pkg: String): String = try {
        val pm = packageManager
        pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
    } catch (e: Exception) { pkg }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
