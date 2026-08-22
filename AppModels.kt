package com.aegis.appblocker.ui

import android.graphics.drawable.Drawable

/** An installed app the user can pick to block. */
data class InstalledApp(
    val packageName: String,
    val label: String,
    val icon: Drawable?
)

/** Permission readiness for the setup checklist. */
data class PermissionState(
    val accessibility: Boolean = false,
    val overlay: Boolean = false,
    val notifications: Boolean = false,
    val vpnReady: Boolean = false
) {
    val allAppBlockingReady get() = accessibility && overlay
}
