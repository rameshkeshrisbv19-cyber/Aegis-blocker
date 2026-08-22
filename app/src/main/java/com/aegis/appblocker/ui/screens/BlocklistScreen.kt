package com.aegis.appblocker.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.ImageView
import com.aegis.appblocker.data.BlockedTarget
import com.aegis.appblocker.ui.*
import com.aegis.appblocker.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlocklistScreen(vm: BlockerViewModel) {
    val targets by vm.targets.collectAsState()
    var showAppPicker by remember { mutableStateOf(false) }
    var showWebAdd by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column {
                    Text("Blocklist", color = AegisTextHi, fontSize = 30.sp, fontWeight = FontWeight.Black)
                    Text("Everything Aegis can guard.", color = AegisTextLo, fontSize = 14.sp)
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AddChip("Add app", Icons.Rounded.Apps, AegisPrimary, Modifier.weight(1f)) {
                        vm.loadInstalledApps(); showAppPicker = true
                    }
                    AddChip("Add website", Icons.Rounded.Language, AegisSecondary, Modifier.weight(1f)) {
                        showWebAdd = true
                    }
                }
            }
            if (targets.isEmpty()) {
                item { EmptyState("No blocks yet", "Add an app or website to start guarding your focus.") }
            }
            items(targets, key = { it.id }) { t ->
                TargetRow(
                    t,
                    onToggle = { vm.toggleTargetEnabled(t) },
                    onAlways = { vm.toggleAlwaysBlock(t) },
                    onDelete = { vm.deleteTarget(t) }
                )
            }
            item { Spacer(Modifier.height(70.dp)) }
        }
    }

    if (showAppPicker) {
        AppPickerSheet(vm, onDismiss = { showAppPicker = false })
    }
    if (showWebAdd) {
        WebsiteAddDialog(
            onAdd = { domain, always -> vm.addWebsite(domain, always); showWebAdd = false },
            onDismiss = { showWebAdd = false }
        )
    }
}

@Composable
private fun AddChip(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Row(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.16f))
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, color = color, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

@Composable
private fun TargetRow(t: BlockedTarget, onToggle: () -> Unit, onAlways: () -> Unit, onDelete: () -> Unit) {
    GlassCard(modifier = Modifier.animateContentSize(), padding = 16.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).clip(CircleShape)
                    .background((if (t.isApp) AegisPrimary else AegisSecondary).copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (t.isApp) Icons.Rounded.Android else Icons.Rounded.Language,
                    null, tint = if (t.isApp) AegisPrimary else AegisSecondary
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(t.label, color = AegisTextHi, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(
                    if (t.alwaysBlock) "Always blocked" else "Blocked by schedule",
                    color = if (t.alwaysBlock) AegisDanger else AegisTextLo, fontSize = 12.sp
                )
            }
            Switch(
                checked = t.enabled, onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(checkedTrackColor = AegisPrimary, checkedThumbColor = Color.White)
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            FilterChip(
                selected = t.alwaysBlock, onClick = onAlways,
                label = { Text("Always block", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Rounded.Lock, null, Modifier.size(16.dp)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AegisDanger.copy(alpha = 0.2f),
                    selectedLabelColor = AegisDanger, selectedLeadingIconColor = AegisDanger
                )
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.DeleteOutline, null, tint = AegisTextLo)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppPickerSheet(vm: BlockerViewModel, onDismiss: () -> Unit) {
    val apps by vm.installedApps.collectAsState()
    var query by remember { mutableStateOf("") }
    var always by remember { mutableStateOf(false) }
    val filtered = remember(apps, query) {
        if (query.isBlank()) apps else apps.filter { it.label.contains(query, true) }
    }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = AegisSurface) {
        Column(Modifier.padding(horizontal = 18.dp).padding(bottom = 20.dp)) {
            Text("Choose an app", color = AegisTextHi, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = query, onValueChange = { query = it },
                placeholder = { Text("Search apps") },
                leadingIcon = { Icon(Icons.Rounded.Search, null) },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                colors = fieldColors()
            )
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(always, { always = it }, colors = CheckboxDefaults.colors(checkedColor = AegisDanger))
                Text("Block always (ignore schedule)", color = AegisTextLo, fontSize = 13.sp)
            }
            if (apps.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(30.dp), Alignment.Center) {
                    CircularProgressIndicator(color = AegisPrimary)
                }
            }
            LazyColumn(Modifier.heightIn(max = 420.dp)) {
                items(filtered, key = { it.packageName }) { app ->
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .clickable { vm.addApp(app, always); onDismiss() }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AndroidView(
                            factory = { ImageView(it) },
                            modifier = Modifier.size(38.dp),
                            update = { iv -> iv.setImageDrawable(app.icon) }
                        )
                        Spacer(Modifier.width(14.dp))
                        Text(app.label, color = AegisTextHi, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun WebsiteAddDialog(onAdd: (String, Boolean) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf("") }
    var always by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AegisSurface,
        title = { Text("Add a website", color = AegisTextHi) },
        text = {
            Column {
                OutlinedTextField(
                    value = text, onValueChange = { text = it },
                    placeholder = { Text("e.g. instagram.com") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    colors = fieldColors(), modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(always, { always = it }, colors = CheckboxDefaults.colors(checkedColor = AegisDanger))
                    Text("Block always", color = AegisTextLo, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onAdd(text, always) }) { Text("Add", color = AegisPrimary) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = AegisTextLo) } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AegisPrimary,
    unfocusedBorderColor = AegisTextLo.copy(alpha = 0.4f),
    focusedTextColor = AegisTextHi,
    unfocusedTextColor = AegisTextHi,
    cursorColor = AegisPrimary
)

@Composable
fun EmptyState(title: String, subtitle: String) {
    GlassCard {
        Column(Modifier.fillMaxWidth().padding(vertical = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Rounded.Inbox, null, tint = AegisTextLo, modifier = Modifier.size(46.dp))
            Spacer(Modifier.height(12.dp))
            Text(title, color = AegisTextHi, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = AegisTextLo, fontSize = 13.sp)
        }
    }
}
