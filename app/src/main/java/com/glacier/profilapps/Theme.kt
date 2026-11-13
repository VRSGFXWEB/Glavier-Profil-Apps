package com.glacier.profilapps

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** dark = pilihan tema, t = nilai animasi 0 (terang) .. 1 (gelap). */
object ThemeState {
    var dark by mutableStateOf(false)
    var t by mutableFloatStateOf(0f)
}

private fun tk(l: Long, d: Long): Color = lerp(Color(l), Color(d), ThemeState.t)

val BgBase: Color get() = tk(0xFFF0F7FD, 0xFF0A1424)
val BgA: Color get() = tk(0xFFFFFFFF, 0xFF0F1D38)
val BgB: Color get() = tk(0xFFE8F4FC, 0xFF0A1426)
val BgC: Color get() = tk(0xFFD8ECF8, 0xFF060E1C)
val IcePrimary: Color get() = tk(0xFF0284C7, 0xFF0EA5E9)
val IceDeep: Color get() = tk(0xFF0369A1, 0xFF7DD3FC)
val IceGlow = Color(0xFF38BDF8)
val IceLight: Color get() = tk(0xFFE0F2FE, 0xFF16294A)
val IceSurface: Color get() = tk(0xFFF0F9FF, 0xFF101E3A)
val IceBorder: Color get() = tk(0xFFBAE6FD, 0xFF24446B)
val IceRim = Color(0xFF7DD3FC)
val TextMain: Color get() = tk(0xFF0C4A6E, 0xFFE0F2FE)
val TextMuted: Color get() = tk(0xFF64748B, 0xFF94A3B8)
val Danger: Color get() = tk(0xFFE11D48, 0xFFFB7185)
val DangerLight: Color get() = tk(0xFFFFE4E6, 0xFF3B1424)
val Success: Color get() = tk(0xFF059669, 0xFF34D399)
val SuccessLight: Color get() = tk(0xFFDCFCE7, 0xFF0F3328)
val Warning: Color get() = tk(0xFFD97706, 0xFFFBBF24)
val WarningLight: Color get() = tk(0xFFFEF3C7, 0xFF3B2C0C)
val ShadowIce: Color get() = tk(0x660284C7, 0x99000000)
val WhiteRim: Color get() = tk(0xF2FFFFFF, 0x33BAE6FD)
val PanelBg: Color get() = tk(0xFFFFFFFF, 0xFF14264A)
val Panel2: Color get() = tk(0xFFE6F4FE, 0xFF0F1F3D)
val HeadA: Color get() = tk(0xE6FFFFFF, 0xE6162B52)
val HeadB: Color get() = tk(0xB3E0F2FE, 0xB30F2040)
val PitA: Color get() = tk(0xBFE0F2FE, 0xBF14294D)
val PitB: Color get() = tk(0x80F0F9FF, 0x800F1F3C)
val PitC: Color get() = tk(0xCCE6F6FF, 0xCC10223F)
val GlassA: Color get() = tk(0xE0FFFFFF, 0xE0172E57)
val GlassB: Color get() = tk(0xA6E0F2FE, 0xA6122545)
val ActA: Color get() = tk(0xEBFFFFFF, 0xEB182F58)
val ActB: Color get() = tk(0xBFF0F9FF, 0xBF112440)
val RowA: Color get() = tk(0xF2FFFFFF, 0xF2182E55)
val RowB: Color get() = tk(0xD9F4FAFF, 0xD9122441)
val BandA: Color get() = tk(0xFFBAE6FD, 0xFF17406B)
val BandB: Color get() = tk(0xFF7DD3FC, 0xFF1F5E92)
val BandC: Color get() = tk(0xFF38BDF8, 0xFF2B82C2)
val SliceBg: Color get() = tk(0xB3E6F4FD, 0xB3101F3E)
val DlgA: Color get() = tk(0xFAFFFFFF, 0xFA14264A)
val DlgB: Color get() = tk(0xF2F0F9FF, 0xF20E1C38)
val NavA: Color get() = tk(0xE6F0F9FF, 0xE6132850)
val NavB: Color get() = tk(0xCCE0F2FE, 0xCC0F2040)
val ToastBg: Color get() = tk(0xF2FFFFFF, 0xF2162B52)
val SwitchOff: Color get() = tk(0xFFCBD5E1, 0xFF334155)
val CancelBg: Color get() = tk(0xFFE2E8F0, 0xFF1E3352)
val CancelFg: Color get() = tk(0xFF334155, 0xFFCBD5E1)
val ChipBadge: Color get() = tk(0x0F000000, 0x22FFFFFF)
val InsetDark: Color get() = tk(0x400284C7, 0x66000000)
val InsetLight: Color get() = tk(0xFFFFFFFF, 0x1AFFFFFF)
val IceBezier = CubicBezierEasing(0.2f, 0.9f, 0.1f, 1f)

@Composable
fun GlacierTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(primary = Color(0xFF0284C7), background = Color(0xFFF0F7FD)),
        content = content
    )
}

/** Efek "tenggelam" (inset shadow) ala CSS. Pasang setelah clip + background. */
fun Modifier.inset(shape: Shape, depth: Dp, dark: Color? = null): Modifier = this.drawBehind {
    val d = dark ?: InsetDark
    val brush = Brush.linearGradient(
        listOf(d, d.copy(alpha = 0f), InsetLight.copy(alpha = 0f), InsetLight),
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
    return this.clip(shape).background(SliceBg).drawBehind {
        val w = 2.dp.toPx(); val ext = 60.dp.toPx()
        val top = if (first) w / 2 else -ext
        val bot = if (last) size.height - w / 2 else size.height + ext
        drawRoundRect(WhiteRim, Offset(w / 2, top), Size(size.width - w, bot - top), CornerRadius(r.toPx()), style = Stroke(w))
    }
}
