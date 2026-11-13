package com.glacier.profilapps

import android.util.LruCache
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun Chip(text: String, badge: Int, selected: Boolean, onClick: () -> Unit) {
    val fg by animateColorAsState(if (selected) Color.White else TextMuted, tween(250), label = "cf")
    val on by animateFloatAsState(if (selected) 1f else 0f, tween(250), label = "co")
    Row(
        Modifier.pressable(onClick).clip(CircleShape)
            .background(PanelBg.copy(alpha = 0.85f))
            .background(Brush.linearGradient(listOf(IcePrimary.copy(alpha = on), IceDeep.copy(alpha = on))))
            .border(1.5.dp, if (selected) IceDeep else IceBorder, CircleShape)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text, color = fg, fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
        Box(Modifier.clip(CircleShape).background(if (selected) Color(0x40FFFFFF) else ChipBadge).padding(horizontal = 6.dp, vertical = 1.dp)) {
            Text("$badge", color = fg, fontSize = 10.sp)
        }
    }
}

@Composable
fun MetricPill(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier.clip(shape).background(Brush.linearGradient(listOf(GlassA, GlassB)))
            .inset(shape, 2.dp).border(1.5.dp, WhiteRim, shape).padding(vertical = 12.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = color, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
        Text(label.uppercase(), color = TextMuted, fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
    }
}

@Composable
fun SearchField(value: String, onChange: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier) {
    var focused by remember { mutableStateOf(false) }
    val bc by animateColorAsState(if (focused) IcePrimary else IceBorder, tween(250), label = "sf")
    Row(
        modifier.fillMaxWidth().clip(CircleShape)
            .background(Brush.linearGradient(listOf(IceSurface, Panel2)))
            .inset(CircleShape, 2.dp).border(1.5.dp, bc, CircleShape).padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        I(Ic.Search, IceDeep, 16.dp)
        Spacer(Modifier.width(10.dp))
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) Text(placeholder, color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            BasicTextField(
                value, onChange, singleLine = true,
                textStyle = TextStyle(color = TextMain, fontSize = 13.6.sp, fontWeight = FontWeight.Bold),
                cursorBrush = SolidColor(IcePrimary),
                modifier = Modifier.fillMaxWidth().onFocusChanged { focused = it.isFocused }
            )
        }
        AnimatedVisibility(value.isNotEmpty(), enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut()) {
            Box(
                Modifier.size(24.dp).clip(CircleShape).background(IceLight).border(1.dp, IceBorder, CircleShape).clickable { onChange("") },
                contentAlignment = Alignment.Center
            ) { I(Ic.Close, IceDeep, 14.dp) }
        }
    }
}

object IconCache {
    private val cache = LruCache<String, ImageBitmap>(300)
    fun get(p: String): ImageBitmap? = cache.get(p)
    fun load(ctx: android.content.Context, p: String): ImageBitmap? = try {
        val b = ctx.packageManager.getApplicationIcon(p).toBitmap(96, 96).asImageBitmap()
        cache.put(p, b); b
    } catch (e: Exception) { null }
}

@Composable
fun AppIcon(pkg: String, size: Dp = 42.dp) {
    val ctx = LocalContext.current
    val bmp by produceState<ImageBitmap?>(IconCache.get(pkg), pkg) {
        if (value == null) value = withContext(Dispatchers.IO) { IconCache.load(ctx, pkg) }
    }
    val b = bmp
    Box(
        Modifier.size(size).shadow(2.dp, RoundedCornerShape(12.dp), ambientColor = ShadowIce, spotColor = ShadowIce)
            .clip(RoundedCornerShape(12.dp)).background(PanelBg)
            .border(1.5.dp, IceBorder, RoundedCornerShape(12.dp)).padding(2.dp)
    ) { if (b != null) Image(b, null, Modifier.fillMaxSize()) }
}

@Composable
fun IceSwitch(checked: Boolean, onChange: (Boolean) -> Unit) {
    val on by animateFloatAsState(if (checked) 1f else 0f, tween(300, easing = IceBezier), label = "sw")
    val x by animateDpAsState(if (checked) 27.dp else 3.dp, tween(300, easing = IceBezier), label = "swx")
    Box(
        Modifier.size(52.dp, 28.dp).clip(CircleShape).background(SwitchOff)
            .background(Brush.horizontalGradient(listOf(IceGlow.copy(alpha = on), IcePrimary.copy(alpha = on))))
            .clickable { onChange(!checked) }
    ) {
        Box(Modifier.offset(x = x, y = 3.dp).size(22.dp).shadow(3.dp, CircleShape).clip(CircleShape).background(Color.White))
    }
}

@Composable
fun SlidingSelector(options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    BoxWithConstraints(
        Modifier.fillMaxWidth().clip(CircleShape).background(IceLight).inset(CircleShape, 2.dp).padding(3.dp)
    ) {
        val pillW = maxWidth / options.size
        val x by animateDpAsState(pillW * selected, tween(300, easing = IceBezier), label = "pill")
        Box(
            Modifier.offset(x = x).size(pillW, 30.dp).shadow(3.dp, CircleShape, ambientColor = IcePrimary, spotColor = IcePrimary)
                .clip(CircleShape).background(Brush.linearGradient(listOf(Color(0xFF0EA5E9), IcePrimary)))
        )
        Row(Modifier.fillMaxWidth()) {
            options.forEachIndexed { i, o ->
                val fg by animateColorAsState(if (i == selected) Color.White else TextMuted, tween(250), label = "sel")
                Box(
                    Modifier.weight(1f).height(30.dp).clip(CircleShape).clickable { onSelect(i) },
                    contentAlignment = Alignment.Center
                ) { Text(o, color = fg, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1) }
            }
        }
    }
}

@Composable
fun IslandToast(vm: MainVM) {
    var show by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf("") }
    LaunchedEffect(vm.toastTick) {
        if (vm.toastTick == 0) return@LaunchedEffect
        text = vm.toastMsg; show = true
        delay(2600)
        show = false
    }
    Box(Modifier.fillMaxWidth().statusBarsPadding().padding(top = 8.dp, start = 16.dp, end = 16.dp), contentAlignment = Alignment.TopCenter) {
        AnimatedVisibility(
            show,
            enter = slideInVertically(spring(0.55f, 280f)) { -it * 2 } + fadeIn() + scaleIn(initialScale = 0.88f),
            exit = slideOutVertically { -it * 2 } + fadeOut() + scaleOut(targetScale = 0.9f)
        ) {
            Row(
                Modifier.shadow(14.dp, CircleShape, ambientColor = ShadowIce, spotColor = ShadowIce).clip(CircleShape)
                    .background(ToastBg).border(1.5.dp, WhiteRim, CircleShape)
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    Modifier.size(28.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Color(0xFF0EA5E9), IcePrimary))),
                    contentAlignment = Alignment.Center
                ) { I(Ic.Check, Color.White, 15.dp) }
                Text(text, color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, maxLines = 2)
            }
        }
    }
}
