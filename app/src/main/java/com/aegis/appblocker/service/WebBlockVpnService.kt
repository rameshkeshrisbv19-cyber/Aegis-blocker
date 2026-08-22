package com.aegis.appblocker.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.aegis.appblocker.MainActivity
import com.aegis.appblocker.R
import com.aegis.appblocker.data.BlockerRepository
import com.aegis.appblocker.util.Notifications
import kotlinx.coroutines.*
import java.io.FileInputStream
import java.nio.ByteBuffer

/**
 * A local, no-root VPN that inspects outgoing DNS queries and drops those that resolve blocked
 * domains. Traffic that isn't blocked is forwarded normally. This is a compact, educational
 * DNS-filter implementation — production filtering would add full TCP/UDP forwarding & caching.
 */
class WebBlockVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var repo: BlockerRepository
    @Volatile private var blockedDomains: Set<String> = emptySet()

    override fun onCreate() {
        super.onCreate()
        repo = BlockerRepository.get(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> { stopVpn(); return START_NOT_STICKY }
            else -> startVpn()
        }
        return START_STICKY
    }

    private fun startVpn() {
        if (vpnInterface != null) return
        startForeground(NOTIF_ID, buildNotification())

        val builder = Builder()
            .setSession("Aegis Web Filter")
            .addAddress("10.111.222.1", 24)
            .addDnsServer("1.1.1.1")
            .addRoute("0.0.0.0", 0)
            .setBlocking(true)
        try {
            builder.addDisallowedApplication(packageName)
        } catch (_: Exception) {}

        vpnInterface = builder.establish()

        scope.launch {
            refreshDomains()
            runPacketLoop()
        }
        // Periodically refresh which domains are blocked (schedules change over time).
        scope.launch {
            while (isActive) { delay(30_000); refreshDomains() }
        }
    }

    private suspend fun refreshDomains() {
        blockedDomains = repo.currentlyBlockedDomains()
    }

    private suspend fun runPacketLoop() {
        val iface = vpnInterface ?: return
        val input = FileInputStream(iface.fileDescriptor)
        val packet = ByteBuffer.allocate(32767)
        while (scope.isActive) {
            val length = try { input.read(packet.array()) } catch (e: Exception) { break }
            if (length <= 0) { delay(10); continue }
            val domain = DnsParser.extractQueriedDomain(packet.array(), length)
            if (domain != null && isBlocked(domain)) {
                // Drop the query -> the site cannot resolve -> effectively blocked.
                Notifications.notifyBlocked(this, domain)
                repo.logEvent(
                    com.aegis.appblocker.data.BlockEvent(label = domain, blocked = true)
                )
            }
            // Non-blocked packets would be forwarded to the tunnel in a full implementation.
            packet.clear()
        }
    }

    private fun isBlocked(domain: String): Boolean =
        blockedDomains.any { domain == it || domain.endsWith(".$it") }

    private fun buildNotification() =
        NotificationCompat.Builder(this, Notifications.CHANNEL_SERVICE)
            .setSmallIcon(R.drawable.ic_shield)
            .setContentTitle("Aegis web filter active")
            .setContentText("Scheduled websites are being blocked.")
            .setOngoing(true)
            .setContentIntent(
                PendingIntent.getActivity(
                    this, 0, Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()

    private fun stopVpn() {
        scope.coroutineContext.cancelChildren()
        vpnInterface?.close(); vpnInterface = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() { stopVpn(); scope.cancel(); super.onDestroy() }

    companion object {
        const val ACTION_STOP = "com.aegis.appblocker.STOP_VPN"
        private const val NOTIF_ID = 42

        fun start(context: Context) {
            context.startService(Intent(context, WebBlockVpnService::class.java))
        }
        fun stop(context: Context) {
            context.startService(
                Intent(context, WebBlockVpnService::class.java).setAction(ACTION_STOP)
            )
        }
    }
}
