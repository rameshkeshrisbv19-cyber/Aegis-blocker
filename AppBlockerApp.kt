package com.aegis.appblocker

import android.app.Application
import com.aegis.appblocker.util.Notifications

class AppBlockerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Notifications.createChannels(this)
    }
}
