package com.aegis.appblocker.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aegis.appblocker.ui.theme.*

/** Frosted glass-style card used across the app. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    padding: Dp = 18.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    androidx.compose.foundation.layout.Column(
        modifier
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF1A2138).copy(alpha = 0.9f), Color(0xFF12172A).copy(alpha = 0.9f))
                )
            )
            .border(
                BorderStroke(1.dp, Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.02f))
                )),
                RoundedCornerShape(22.dp)
            )
            .padding(padding),
        content = content
    )
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text, modifier = modifier,
        color = AegisTextHi, fontSize = 20.sp, fontWeight = FontWeight.Bold
    )
}

@Composable
fun Pill(text: String, color: Color) {
    Box(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.18f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

/** Shimmering gradient border used for the hero card. */
@Composable
fun rememberShimmer(): Float {
    val t = rememberInfiniteTransition(label = "shimmer")
    val v by t.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(3500, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmerV"
    )
    return v
}
