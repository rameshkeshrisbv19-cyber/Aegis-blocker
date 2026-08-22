package com.aegis.appblocker.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.item
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.aegis.appblocker.Tab
import com.aegis.appblocker.service.WebBlockVpnService
import com.aegis.appblocker.ui.*
import com.aegis.appblocker.ui.theme.*
import com.aegis.appblocker.util.Permissions

@Composable
fun HomeScreen(vm: BlockerViewModel, onNavigate: (Tab) -> Unit) {
    val context = LocalContext.current
    val targets by vm.targets.collectAsState()
    val schedules by vm.schedules.collectAsState()

    var perms by remember { mutableStateOf(readPerms(context)) }
    // Re-check permissions whenever we return to the app.
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val obs = LifecycleEventObserver { _, e ->
            if (e == Lifecycle.Event.ON_RESUME) perms = readPerms(context)
        }
        owner.lifecycle.addObserver(obs)
        onDispose { owner.lifecycle.removeObserver(obs) }
    }

    val appCount = targets.count { it.isApp }
    val siteCount = targets.count { !it.isApp }
    val active = perms.accessibility

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Header() }
        item { HeroCard(active = active, blocked = targets.size) }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                StatCard("Apps", appCount.toString(), Icons.Rounded.Apps, AegisPrimary, Modifier.weight(1f))
                StatCard("Websites", siteCount.toString(), Icons.Rounded.Language, AegisSecondary, Modifier.weight(1f))
                StatCard("Schedules", schedules.size.toString(), Icons.Rounded.Schedule, AegisAccent, Modifier.weight(1f))
            }
        }

        item {
            val incomplete = !perms.accessibility || !perms.overlay || !perms.notifications
            AnimatedVisibility(incomplete) {
                SetupChecklist(perms = perms, context = context)
            }
        }

        item { WebFilterCard(perms.vpnReady, context) }

        item {
            QuickActions(
                onAddApp = { onNavigate(Tab.Blocked) },
                onSchedule = { onNavigate(Tab.Schedule) }
            )
        }
        item { Spacer(Modifier.height(70.dp)) }
    }
}

private fun readPerms(context: Context) = PermissionState(
    accessibility = Permissions.isAccessibilityEnabled(context),
    overlay = Permissions.canDrawOverlays(context),
    notifications = true,
    vpnReady = Permissions.isVpnPrepared(context)
)

@Composable
private fun Header() {
    Column {
        Text("Aegis", color = AegisTextHi, fontSize = 34.sp, fontWeight = FontWeight.Black)
        Text("Guard your attention.", color = AegisTextLo, fontSize = 15.sp)
    }
}

@Composable
private fun HeroCard(active: Boolean, blocked: Int) {
    val transition = rememberInfiniteTransition(label = "hero")
    val angle by transition.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(9000, easing = LinearEasing)),
        label = "angle"
    )
    GlassCard(padding = 24.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    Modifier
                        .size(96.dp)
                        .rotate(angle)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                listOf(
                                    AegisPrimary, AegisSecondary, AegisAccent, AegisPrimary
                                )
                            )
                        )
                )
                Box(
                    Modifier.size(84.dp).clip(CircleShape).background(AegisSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Shield, null,
                        tint = if (active) AegisSecondary else AegisTextLo,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            Spacer(Modifier.width(20.dp))
            Column {
                Pill(
                    if (active) "PROTECTION ACTIVE" else "PROTECTION OFF",
                    if (active) AegisSecondary else AegisDanger
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    if (active) "You're shielded" else "Turn on shield",
                    color = AegisTextHi, fontSize = 22.sp, fontWeight = FontWeight.Bold
                )
                Text(
                    "$blocked target(s) under guard",
                    color = AegisTextLo, fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier) {
    GlassCard(modifier = modifier, padding = 16.dp) {
        Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(10.dp))
        Text(value, color = AegisTextHi, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(label, color = AegisTextLo, fontSize = 12.sp)
    }
}

@Composable
private fun SetupChecklist(perms: PermissionState, context: Context) {
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Tune, null, tint = AegisAccent)
            Spacer(Modifier.width(10.dp))
            Text("Finish setup", color = AegisTextHi, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(4.dp))
        Text("Grant these so Aegis can block apps reliably.", color = AegisTextLo, fontSize = 13.sp)
        Spacer(Modifier.height(14.dp))

        PermissionRow(
            "Accessibility service", "Detects when a blocked app opens",
            perms.accessibility
        ) { context.startActivity(Permissions.accessibilityIntent()) }
        Spacer(Modifier.height(10.dp))
        PermissionRow(
            "Display over other apps", "Shows the block screen on top",
            perms.overlay
        ) { context.startActivity(Permissions.overlayIntent(context.packageName)) }
    }
}

@Composable
private fun PermissionRow(title: String, subtitle: String, granted: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AegisSurfaceHi.copy(alpha = 0.5f))
            .clickable(enabled = !granted, onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (granted) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
            null,
            tint = if (granted) AegisSecondary else AegisTextLo
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = AegisTextHi, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = AegisTextLo, fontSize = 12.sp)
        }
        if (!granted) Icon(Icons.Rounded.ChevronRight, null, tint = AegisPrimary)
    }
}

@Composable
private fun WebFilterCard(vpnReady: Boolean, context: Context) {
    var running by remember { mutableStateOf(false) }
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Public, null, tint = AegisSecondary)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("Website filter", color = AegisTextHi, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text(
                    if (running) "Filtering active" else "Blocks scheduled sites via local VPN",
                    color = AegisTextLo, fontSize = 12.sp
                )
            }
            Switch(
                checked = running,
                onCheckedChange = { on ->
                    running = on
                    if (on) {
                        val prep = android.net.VpnService.prepare(context)
                        if (prep != null) context.startActivity(prep)
                        else WebBlockVpnService.start(context)
                    } else WebBlockVpnService.stop(context)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = AegisSecondary
                )
            )
        }
    }
}

@Composable
private fun QuickActions(onAddApp: () -> Unit, onSchedule: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        ActionButton("Add block", Icons.Rounded.AddCircle, AegisPrimary, Modifier.weight(1f), onAddApp)
        ActionButton("New schedule", Icons.Rounded.MoreTime, AegisAccent, Modifier.weight(1f), onSchedule)
    }
}

@Composable
private fun ActionButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(listOf(color.copy(alpha = 0.9f), color.copy(alpha = 0.6f))))
            .clickable(onClick = onClick)
            .padding(vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = Color.White)
            Spacer(Modifier.height(6.dp))
            Text(label, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}
