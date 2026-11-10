package com.glacier.profilapps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/** Muncul pelan-pelan (fade + naik) dengan delay berurutan. */
@Composable
fun Appear(index: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(index * 90L); visible = true }
    AnimatedVisibility(
        visible,
        enter = fadeIn(tween(550)) +
            slideInVertically(tween(550, easing = FastOutSlowInEasing)) { it / 5 }
    ) { content() }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val src = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (pressed) 0.97f else 1f,
        spring(dampingRatio = 0.55f, stiffness = 400f), label = "card"
    )
    val shape = RoundedCornerShape(22.dp)
    var m = modifier
        .fillMaxWidth()
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .clip(shape)
        .background(Brush.linearGradient(listOf(Color(0x33BFE6FF), Color(0x110D1B3D))))
        .border(1.dp, Color(0x44BFE6FF), shape)
    if (onClick != null) m = m.clickable(interactionSource = src, indication = null, onClick = onClick)
    Column(m.padding(18.dp), content = content)
}

@Composable
fun GlowButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val src = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (pressed) 0.94f else 1f, spring(dampingRatio = 0.5f, stiffness = 500f), label = "btn"
    )
    val glow by rememberInfiniteTransition(label = "glow").animateFloat(
        0.55f, 1f, infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glowA"
    )
    Box(
        modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.horizontalGradient(listOf(AccentDeep, Accent)))
            .border(1.dp, Ice.copy(alpha = glow * 0.7f), RoundedCornerShape(16.dp))
            .clickable(interactionSource = src, indication = null, onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

private class Flake(
    val x: Float, val y: Float, val fall: Int, val sway: Int,
    val phase: Float, val r: Float, val a: Float
)

/** Salju jatuh halus, loop mulus. */
@Composable
fun Snow(modifier: Modifier = Modifier) {
    val flakes = remember {
        List(45) {
            Flake(
                Random.nextFloat(), Random.nextFloat(), 1 + Random.nextInt(3), 1 + Random.nextInt(2),
                Random.nextFloat() * 6.28f, 1.5f + Random.nextFloat() * 3.5f, 0.25f + Random.nextFloat() * 0.5f
            )
        }
    }
    val t by rememberInfiniteTransition(label = "snow").animateFloat(
        0f, 1f, infiniteRepeatable(tween(30000, easing = LinearEasing)), label = "t"
    )
    Canvas(modifier) {
        flakes.forEach { f ->
            val y = ((f.y + t * f.fall) % 1f) * size.height
            val x = (f.x + sin(2f * PI.toFloat() * t * f.sway + f.phase) * 0.02f) * size.width
            drawCircle(Color.White.copy(alpha = f.a), f.r.dp.toPx(), Offset(x, y))
        }
    }
}
