package com.glacier.profilapps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun Appear(index: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(index * 70L); visible = true }
    AnimatedVisibility(
        visible,
        enter = fadeIn(tween(500)) + slideInVertically(tween(500, easing = IceBezier)) { it / 6 }
    ) { content() }
}

@Composable
fun PageBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier.fillMaxSize().drawBehind {
            drawRect(
                Brush.radialGradient(
                    listOf(BgA, BgB, BgC),
                    center = Offset(size.width * 0.15f, size.height * 0.15f), radius = size.maxDimension * 1.2f
                )
            )
        },
        content = content
    )
}

@Composable
fun I(icon: ImageVector, tint: Color, size: Dp = 16.dp, modifier: Modifier = Modifier) =
    Icon(icon, null, tint = tint, modifier = modifier.size(size))

@Composable
fun SyncButton(label: String, onClick: () -> Unit) {
    val rot = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    Row(
        Modifier.pressable {
            scope.launch { rot.snapTo(0f); rot.animateTo(720f, tween(650, easing = LinearEasing)) }
            onClick()
        }.shadow(3.dp, CircleShape, ambientColor = ShadowIce, spotColor = ShadowIce).clip(CircleShape)
            .background(PanelBg).border(1.5.dp, IceBorder, CircleShape).padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        I(Ic.Sync, IceDeep, 16.dp, Modifier.graphicsLayer { rotationZ = rot.value })
        Text(label, color = IceDeep, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun HeaderCapsule(icon: ImageVector, title: String, sub: String, action: String? = null, onAction: () -> Unit = {}) {
    Row(
        Modifier.fillMaxWidth().padding(bottom = 14.dp)
            .shadow(6.dp, CircleShape, ambientColor = ShadowIce, spotColor = ShadowIce).clip(CircleShape)
            .background(Brush.linearGradient(listOf(HeadA, HeadB)))
            .inset(CircleShape, 2.dp).border(1.5.dp, WhiteRim, CircleShape)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(38.dp).clip(RoundedCornerShape(12.dp))
                .background(Brush.linearGradient(listOf(IceLight, IceBorder)))
                .border(1.5.dp, IcePrimary, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) { I(icon, IceDeep, 16.dp) }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title, maxLines = 1,
                style = TextStyle(
                    brush = Brush.horizontalGradient(listOf(TextMain, IcePrimary)),
                    fontSize = 17.sp, fontWeight = FontWeight.ExtraBold
                )
            )
            Text(sub.uppercase(), color = IcePrimary, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp, maxLines = 1)
        }
        if (action != null) { SyncButton(action, onAction); Spacer(Modifier.width(8.dp)) }
        ThemeToggle()
    }
}

@Composable
fun ThemeToggle() {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    Box(
        Modifier.pressable {
            ThemeState.dark = !ThemeState.dark
            Store(ctx).dark = ThemeState.dark
        }.shadow(3.dp, CircleShape, ambientColor = ShadowIce, spotColor = ShadowIce).clip(CircleShape)
            .background(PanelBg).border(1.5.dp, IceBorder, CircleShape).size(34.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.animation.Crossfade(ThemeState.dark, animationSpec = tween(300), label = "themeic") { d ->
            I(if (d) Ic.Sun else Ic.Moon, IceDeep, 16.dp)
        }
    }
}

/** capsule-table-enclosure */
@Composable
fun PitBox(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(26.dp)
    Column(
        modifier.fillMaxWidth().clip(shape)
            .background(Brush.linearGradient(listOf(PitA, PitB, PitC)))
            .inset(shape, 5.dp).border(2.dp, WhiteRim, shape).padding(horizontal = 10.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp), content = content
    )
}

/** home-info-box */
@Composable
fun GlassBox(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(22.dp)
    Column(
        modifier.clip(shape)
            .background(Brush.linearGradient(listOf(GlassA, GlassB)))
            .inset(shape, 4.dp).border(1.5.dp, WhiteRim, shape).padding(horizontal = 12.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp), content = content
    )
}

/** home-card-action */
@Composable
fun ActionCard(modifier: Modifier = Modifier, onClick: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(22.dp)
    Column(
        modifier.pressable(onClick).shadow(4.dp, shape, ambientColor = ShadowIce, spotColor = ShadowIce).clip(shape)
            .background(Brush.linearGradient(listOf(ActA, ActB)))
            .inset(shape, 2.5.dp).border(1.5.dp, IceBorder, shape).padding(horizontal = 14.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp), content = content
    )
}

/** Pill besar (reset / panduan) */
@Composable
fun PillButton(onClick: () -> Unit, tint: Color, border: Color, borderW: Dp = 1.5.dp, content: @Composable RowScope.() -> Unit) {
    Row(
        Modifier.fillMaxWidth().pressable(onClick)
            .shadow(3.dp, CircleShape, ambientColor = tint, spotColor = tint).clip(CircleShape)
            .background(Brush.linearGradient(listOf(ActA, tint.copy(alpha = 0.12f).compositeOver(ActA))))
            .inset(CircleShape, 2.dp).border(borderW, border, CircleShape).padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically, content = content
    )
}

@Composable
fun TableHead(left: String, right: @Composable () -> Unit = {}) {
    Row(
        Modifier.fillMaxWidth().shadow(3.dp, CircleShape, ambientColor = ShadowIce, spotColor = ShadowIce).clip(CircleShape)
            .background(Brush.linearGradient(listOf(BandA, BandB, BandC)))
            .border(1.5.dp, WhiteRim, CircleShape).padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(left.uppercase(), color = TextMain, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.6.sp, maxLines = 1, modifier = Modifier.weight(1f, false))
        right()
    }
}

@Composable
fun CapsuleRow(modifier: Modifier = Modifier, height: Dp = 68.dp, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier.fillMaxWidth().height(height).clip(CircleShape)
            .background(Brush.verticalGradient(listOf(RowA, RowB)))
            .inset(CircleShape, 2.dp).border(1.5.dp, IceBorder, CircleShape).padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically, content = content
    )
}

enum class BtnKind { Danger, Success, Primary }

@Composable
fun Btn(text: String, kind: BtnKind = BtnKind.Primary, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val (brush, glow) = when (kind) {
        BtnKind.Danger -> Brush.linearGradient(listOf(Color(0xFFF43F5E), Danger)) to Danger
        BtnKind.Success -> Brush.linearGradient(listOf(Color(0xFF10B981), Success)) to Success
        BtnKind.Primary -> Brush.linearGradient(listOf(Color(0xFF0EA5E9), IcePrimary)) to IcePrimary
    }
    Box(
        modifier.pressable(onClick).shadow(4.dp, CircleShape, ambientColor = glow, spotColor = glow).clip(CircleShape)
            .background(brush).padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) { Text(text, color = Color.White, fontSize = 11.7.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1) }
}

@Composable
fun Tag(text: String, bg: Color, fg: Color, border: Color? = null) {
    var m = Modifier.clip(CircleShape).background(bg)
    if (border != null) m = m.border(1.dp, border, CircleShape)
    Box(m.padding(horizontal = 6.dp, vertical = 1.dp)) {
        Text(text, color = fg, fontSize = 9.3.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
    }
}
