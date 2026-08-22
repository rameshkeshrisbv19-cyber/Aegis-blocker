package com.aegis.appblocker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.aegis.appblocker.data.BlockedTarget
import com.aegis.appblocker.data.Schedule
import com.aegis.appblocker.ui.*
import com.aegis.appblocker.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ScheduleScreen(vm: BlockerViewModel) {
    val schedules by vm.schedules.collectAsState()
    val targets by vm.targets.collectAsState()
    var editing by remember { mutableStateOf<Schedule?>(null) }
    var showEditor by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column {
                    Text("Timetable", color = AegisTextHi, fontSize = 30.sp, fontWeight = FontWeight.Black)
                    Text("When apps & sites get blocked.", color = AegisTextLo, fontSize = 14.sp)
                }
            }
            item { WeekOverview(schedules) }
            if (schedules.isEmpty()) {
                item { EmptyState("No schedules", "Create a timetable window like 'Study 9\u20135 PM'.") }
            }
            items(schedules, key = { it.id }) { s ->
                ScheduleCard(
                    s,
                    onEdit = { editing = s; showEditor = true },
                    onToggle = { vm.toggleScheduleEnabled(s) },
                    onDelete = { vm.deleteSchedule(s) }
                )
            }
            item { Spacer(Modifier.height(80.dp)) }
        }

        FloatingActionButton(
            onClick = { editing = null; showEditor = true },
            containerColor = AegisPrimary,
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp).padding(bottom = 60.dp)
        ) { Icon(Icons.Rounded.Add, null, tint = Color.White) }
    }

    if (showEditor) {
        ScheduleEditor(
            existing = editing,
            allTargets = targets,
            vm = vm,
            onDismiss = { showEditor = false },
            onSave = { schedule, ids -> vm.saveSchedule(schedule, ids); showEditor = false }
        )
    }
}

@Composable
private fun WeekOverview(schedules: List<Schedule>) {
    GlassCard {
        Text("This week", color = AegisTextHi, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            for (d in 0..6) {
                val hasBlock = schedules.any { it.enabled && (it.daysMask shr d) and 1 == 1 }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier.size(34.dp).clip(CircleShape)
                            .background(if (hasBlock) AegisPrimary else AegisSurfaceHi),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            TimeUtils.dayLabel(d).first().toString(),
                            color = if (hasBlock) Color.White else AegisTextLo,
                            fontWeight = FontWeight.Bold, fontSize = 13.sp
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Box(
                        Modifier.size(5.dp).clip(CircleShape)
                            .background(if (hasBlock) AegisSecondary else Color.Transparent)
                    )
                }
            }
        }
    }
}

@Composable
private fun ScheduleCard(s: Schedule, onEdit: () -> Unit, onToggle: () -> Unit, onDelete: () -> Unit) {
    GlassCard(modifier = Modifier.clickable(onClick = onEdit)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(10.dp).clip(CircleShape).background(Color(s.accentColor))
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(s.name, color = AegisTextHi, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text(
                    "${TimeUtils.minuteToText(s.startMinuteOfDay)} \u2013 ${TimeUtils.minuteToText(s.endMinuteOfDay)}",
                    color = AegisTextLo, fontSize = 13.sp
                )
            }
            Switch(
                checked = s.enabled, onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(checkedTrackColor = AegisPrimary, checkedThumbColor = Color.White)
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Pill(TimeUtils.daysMaskToText(s.daysMask), AegisSecondary)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onDelete) { Icon(Icons.Rounded.DeleteOutline, null, tint = AegisTextLo) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleEditor(
    existing: Schedule?,
    allTargets: List<BlockedTarget>,
    vm: BlockerViewModel,
    onDismiss: () -> Unit,
    onSave: (Schedule, List<Long>) -> Unit
) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf(existing?.name ?: "Focus time") }
    var start by remember { mutableStateOf(existing?.startMinuteOfDay ?: 9 * 60) }
    var end by remember { mutableStateOf(existing?.endMinuteOfDay ?: 17 * 60) }
    var days by remember { mutableStateOf(existing?.daysMask ?: 0b0011111) }
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }
    var showStart by remember { mutableStateOf(false) }
    var showEnd by remember { mutableStateOf(false) }

    LaunchedEffect(existing?.id) {
        existing?.let { selectedIds = vm.scheduleTargetIds(it.id).toSet() }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = AegisSurface) {
        LazyColumn(
            Modifier.padding(horizontal = 20.dp).padding(bottom = 24.dp).heightIn(max = 640.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    if (existing == null) "New schedule" else "Edit schedule",
                    color = AegisTextHi, fontSize = 22.sp, fontWeight = FontWeight.Bold
                )
            }
            item {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Name") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(), colors = fieldColors()
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TimeBox("Start", start, Modifier.weight(1f)) { showStart = true }
                    TimeBox("End", end, Modifier.weight(1f)) { showEnd = true }
                }
            }
            item {
                Column {
                    Text("Repeat on", color = AegisTextLo, fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        for (d in 0..6) {
                            val on = (days shr d) and 1 == 1
                            Box(
                                Modifier.size(40.dp).clip(CircleShape)
                                    .background(if (on) AegisPrimary else AegisSurfaceHi)
                                    .clickable { days = days xor (1 shl d) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    TimeUtils.dayLabel(d).first().toString(),
                                    color = if (on) Color.White else AegisTextLo,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            item {
                Text("Apply to", color = AegisTextLo, fontSize = 13.sp)
            }
            if (allTargets.isEmpty()) {
                item { Text("Add apps or websites first in the Blocklist tab.", color = AegisTextLo, fontSize = 13.sp) }
            }
            items(allTargets, key = { it.id }) { t ->
                val checked = t.id in selectedIds
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(AegisSurfaceHi.copy(alpha = 0.4f))
                        .clickable {
                            selectedIds = if (checked) selectedIds - t.id else selectedIds + t.id
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (t.isApp) Icons.Rounded.Android else Icons.Rounded.Language, null,
                        tint = if (t.isApp) AegisPrimary else AegisSecondary, modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(t.label, color = AegisTextHi, fontSize = 15.sp, modifier = Modifier.weight(1f))
                    Checkbox(checked, { c -> selectedIds = if (c) selectedIds + t.id else selectedIds - t.id },
                        colors = CheckboxDefaults.colors(checkedColor = AegisPrimary))
                }
            }
            item {
                Button(
                    onClick = {
                        val schedule = (existing ?: Schedule(name = name, startMinuteOfDay = start, endMinuteOfDay = end, daysMask = days))
                            .copy(name = name, startMinuteOfDay = start, endMinuteOfDay = end, daysMask = days)
                        onSave(schedule, selectedIds.toList())
                    },
                    enabled = name.isNotBlank() && days != 0,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AegisPrimary)
                ) { Text("Save schedule", fontWeight = FontWeight.SemiBold) }
            }
        }
    }

    if (showStart) TimePickerDialog(start, { start = it; showStart = false }, { showStart = false })
    if (showEnd) TimePickerDialog(end, { end = it; showEnd = false }, { showEnd = false })
}

@Composable
private fun TimeBox(label: String, minute: Int, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier.clip(RoundedCornerShape(14.dp)).background(AegisSurfaceHi.copy(alpha = 0.5f))
            .clickable(onClick = onClick).padding(14.dp)
    ) {
        Text(label, color = AegisTextLo, fontSize = 12.sp)
        Spacer(Modifier.height(4.dp))
        Text(TimeUtils.minuteToText(minute), color = AegisTextHi, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(initialMinute: Int, onConfirm: (Int) -> Unit, onDismiss: () -> Unit) {
    val state = rememberTimePickerState(
        initialHour = initialMinute / 60, initialMinute = initialMinute % 60, is24Hour = false
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AegisSurface,
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour * 60 + state.minute) }) {
                Text("OK", color = AegisPrimary)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = AegisTextLo) } },
        text = {
            Box(Modifier.fillMaxWidth(), Alignment.Center) {
                TimePicker(
                    state = state,
                    colors = TimePickerDefaults.colors(
                        selectorColor = AegisPrimary,
                        periodSelectorSelectedContainerColor = AegisPrimary.copy(alpha = 0.3f),
                        timeSelectorSelectedContainerColor = AegisPrimary.copy(alpha = 0.3f)
                    )
                )
            }
        }
    )
}
