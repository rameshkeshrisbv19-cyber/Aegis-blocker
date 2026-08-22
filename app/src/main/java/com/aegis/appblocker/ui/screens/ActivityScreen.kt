package com.aegis.appblocker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aegis.appblocker.data.BlockEvent
import com.aegis.appblocker.ui.*
import com.aegis.appblocker.ui.theme.*

@Composable
fun ActivityScreen(vm: BlockerViewModel) {
    val events by vm.events.collectAsState()

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Activity", color = AegisTextHi, fontSize = 30.sp, fontWeight = FontWeight.Black)
                    Text("Recent blocks & unblocks.", color = AegisTextLo, fontSize = 14.sp)
                }
                if (events.isNotEmpty()) {
                    TextButton(onClick = { vm.clearEvents() }) {
                        Text("Clear", color = AegisTextLo)
                    }
                }
            }
        }
        if (events.isEmpty()) {
            item { EmptyState("No activity yet", "Block events will appear here as they happen.") }
        }
        items(events, key = { it.id }) { e -> EventRow(e) }
        item { Spacer(Modifier.height(70.dp)) }
    }
}

@Composable
private fun EventRow(e: BlockEvent) {
    val color = if (e.blocked) AegisDanger else AegisSecondary
    GlassCard(padding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(color.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (e.blocked) Icons.Rounded.Block else Icons.Rounded.LockOpen,
                    null, tint = color, modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "${if (e.blocked) "Blocked" else "Unblocked"} ${e.label}",
                    color = AegisTextHi, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1
                )
                Text(TimeUtils.eventTime(e.timestamp), color = AegisTextLo, fontSize = 12.sp)
            }
            Pill(if (e.blocked) "BLOCK" else "OPEN", color)
        }
    }
}
