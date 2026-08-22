package com.aegis.appblocker.util

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Process
import android.provider.Settings
import android.text.TextUtils
import com.aegis.appblocker.service.AppBlockAccessibilityService

object Permissions {

    fun isAccessibilityEnabled(context: Context): Boolean {
        val expected = "${context.packageName}/${AppBlockAccessibilityService::class.java.name}"
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val splitter = TextUtils.SimpleStringSplitter(':')
        splitter.setString(enabled)
        while (splitter.hasNext()) {
            if (splitter.next().equals(expected, ignoreCase = true)) return true
        }
        return false
    }

    fun canDrawOverlays(context: Context): Boolean = Settings.canDrawOverlays(context)

    fun hasUsageAccess(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(), context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun isVpnPrepared(context: Context): Boolean = VpnService.prepare(context) == null

    // Intents to open the relevant settings screens
    fun accessibilityIntent() = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    fun overlayIntent(pkg: String) =
        Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, android.net.Uri.parse("package:$pkg"))
    fun usageAccessIntent() = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
}
