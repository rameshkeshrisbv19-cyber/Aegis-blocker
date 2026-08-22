package com.aegis.appblocker.block

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aegis.appblocker.ui.theme.AegisTheme

/**
 * The full-screen "You are blocked" wall shown over any blocked app.
 * Pressing back / continue just sends the user home — the app stays behind the wall.
 */
class BlockScreenActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        val label = intent.getStringExtra(EXTRA_LABEL) ?: "This app"

        setContent {
            AegisTheme {
                BlockWall(label = label, onDismiss = { goHome() })
            }
        }
    }

    override fun onBackPressed() { goHome() }

    private fun goHome() {
        startActivity(Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
        finish()
    }

    companion object {
        const val EXTRA_LABEL = "extra_label"
        const val EXTRA_PACKAGE = "extra_package"
    }
}

@Composable
private fun BlockWall(label: String, onDismiss: () -> Unit) {
    val infinite = rememberInfiniteTransition(label = "pulse")
    val pulse by infinite.animateFloat(
        initialValue = 0.92f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(1400, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "scale"
    )
    val glow by infinite.animateFloat(
        initialValue = 0.25f, targetValue = 0.6f,
        animationSpec = infiniteRepeatable(tween(1400, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "glow"
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF120A24), Color(0xFF0A0E1A), Color(0xFF04060D))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    Modifier
                        .size(180.dp)
                        .scale(pulse)
                        .clip(CircleShape)
                        .background(Color(0xFF6C63FF).copy(alpha = glow * 0.4f))
                )
                Box(
                    Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(Color(0xFF6C63FF), Color(0xFF9D4EDD)))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🛡️", fontSize = 54.sp)
                }
            }
            Spacer(Modifier.height(40.dp))
            Text(
                "Blocked by Aegis",
                color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "$label is blocked right now.",
                color = Color(0xFFB9B4E6), fontSize = 18.sp, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Take a breath — your future self will thank you.",
                color = Color(0xFF6B6890), fontSize = 14.sp, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(48.dp))
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF)),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Got it, take me home", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
