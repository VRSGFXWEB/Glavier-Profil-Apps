package com.glacier.profilapps

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val BgBase = Color(0xFFF0F7FD)
val IcePrimary = Color(0xFF0284C7)
val IceDeep = Color(0xFF0369A1)
val IceGlow = Color(0xFF38BDF8)
val IceLight = Color(0xFFE0F2FE)
val IceSurface = Color(0xFFF0F9FF)
val IceBorder = Color(0xFFBAE6FD)
val IceRim = Color(0xFF7DD3FC)
val TextMain = Color(0xFF0C4A6E)
val TextMuted = Color(0xFF64748B)
val Danger = Color(0xFFE11D48)
val DangerLight = Color(0xFFFFE4E6)
val Success = Color(0xFF059669)
val SuccessLight = Color(0xFFDCFCE7)
val Warning = Color(0xFFD97706)
val WarningLight = Color(0xFFFEF3C7)
val ShadowIce = Color(0x660284C7)
val WhiteRim = Color(0xF2FFFFFF)
val IceBezier = CubicBezierEasing(0.2f, 0.9f, 0.1f, 1f)

@Composable
fun GlacierTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = IcePrimary, background = BgBase, surface = IceSurface,
            onSurface = TextMain, onBackground = TextMain
        ),
        content = content
    )
}

/** Efek "tenggelam" (inset shadow) ala CSS. Pasang setelah clip + background. */
fun Modifier.inset(shape: Shape, depth: Dp, dark: Color = Color(0x400284C7)): Modifier = this.drawBehind {
    val brush = Brush.linearGradient(
        listOf(dark, dark.copy(alpha = 0f), Color(0x00FFFFFF), Color.White),
        start = Offset.Zero, end = Offset(size.width, size.height)
    )
    val st = Stroke(depth.toPx() * 2)
    when (val o = shape.createOutline(size, layoutDirection, this)) {
        is Outline.Rectangle -> drawRect(brush, o.rect.topLeft, o.rect.size, style = st)
        is Outline.Rounded -> drawPath(Path().apply { addRoundRect(o.roundRect) }, brush, style = st)
        is Outline.Generic -> drawPath(o.path, brush, style = st)
    }
}

/** Efek tekan: scale 0.94 + turun 2dp. Taruh paling awal di chain modifier. */
fun Modifier.pressable(onClick: () -> Unit): Modifier = composed {
    val src = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    val sc by animateFloatAsState(if (pressed) 0.94f else 1f, spring(dampingRatio = 0.6f, stiffness = 600f), label = "press")
    graphicsLayer { scaleX = sc; scaleY = sc; translationY = if (pressed) 2f * density else 0f }
        .clickable(interactionSource = src, indication = null, onClick = onClick)
}

/** Potongan "capsule-table-enclosure" untuk list lazy. */
fun Modifier.pitSlice(first: Boolean, last: Boolean): Modifier {
    val r = 26.dp
    val shape = RoundedCornerShape(
        topStart = if (first) r else 0.dp, topEnd = if (first) r else 0.dp,
        bottomStart = if (last) r else 0.dp, bottomEnd = if (last) r else 0.dp
    )
    return this.clip(shape).background(Color(0xB3E6F4FD)).drawBehind {
        val w = 2.dp.toPx(); val ext = 60.dp.toPx()
        val top = if (first) w / 2 else -ext
        val bot = if (last) size.height - w / 2 else size.height + ext
        drawRoundRect(WhiteRim, Offset(w / 2, top), Size(size.width - w, bot - top), CornerRadius(r.toPx()), style = Stroke(w))
    }
}
