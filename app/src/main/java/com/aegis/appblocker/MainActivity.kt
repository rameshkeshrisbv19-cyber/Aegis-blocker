package com.aegis.appblocker

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aegis.appblocker.ui.BlockerViewModel
import com.aegis.appblocker.ui.screens.*
import com.aegis.appblocker.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AegisTheme { AegisApp() } }
    }
}

enum class Tab(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Rounded.Dashboard),
    Blocked("Blocklist", Icons.Rounded.Block),
    Schedule("Schedule", Icons.Rounded.CalendarMonth),
    Activity("Activity", Icons.Rounded.History)
}

@Composable
fun AegisApp(vm: BlockerViewModel = viewModel()) {
    var tab by remember { mutableStateOf(Tab.Home) }

    // Ask for notification permission on launch (Android 13+)
    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        containerColor = AegisBg,
        bottomBar = { AegisBottomBar(tab) { tab = it } }
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            AnimatedContent(
                targetState = tab,
                transitionSpec = {
                    (fadeIn(tween(220)) + slideInVertically { it / 12 })
                        .togetherWith(fadeOut(tween(120)))
                },
                label = "tab"
            ) { current ->
                when (current) {
                    Tab.Home -> HomeScreen(vm) { tab = it }
                    Tab.Blocked -> BlocklistScreen(vm)
                    Tab.Schedule -> ScheduleScreen(vm)
                    Tab.Activity -> ActivityScreen(vm)
                }
            }
        }
    }
}

@Composable
private fun AegisBottomBar(selected: Tab, onSelect: (Tab) -> Unit) {
    NavigationBar(
        containerColor = AegisSurface,
        tonalElevation = 0.dp
    ) {
        Tab.values().forEach { t ->
            NavigationBarItem(
                selected = selected == t,
                onClick = { onSelect(t) },
                icon = { Icon(t.icon, contentDescription = t.label) },
                label = { Text(t.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AegisPrimary,
                    selectedTextColor = AegisPrimary,
                    indicatorColor = AegisPrimary.copy(alpha = 0.16f),
                    unselectedIconColor = AegisTextLo,
                    unselectedTextColor = AegisTextLo
                )
            )
        }
    }
}
