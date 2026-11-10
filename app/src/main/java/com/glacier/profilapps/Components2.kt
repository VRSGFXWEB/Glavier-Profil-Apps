package com.glacier.profilapps

import android.util.LruCache
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class BtnKind { Danger, Success, Primary }

@Composable
fun SmallBtn(text: String, kind: BtnKind = BtnKind.Primary, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val src = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.92f else 1f, spring(0.5f, 500f), label = "sb")
    val brush = when (kind) {
        BtnKind.Danger -> Brush.horizontalGradient(listOf(Color(0xFFF43F5E), Color(0xFFE11D48)))
        BtnKind.Success -> Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF059669)))
        BtnKind.Primary -> Brush.horizontalGradient(listOf(AccentDeep, Accent))
    }
    Box(
        modifier.graphicsLayer { scaleX = scale; scaleY = scale }.clip(CircleShape).background(brush)
            .clickable(interactionSource = src, indication = null, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) { Text(text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1) }
}

@Composable
fun ScreenHeader(title: String, sub: String, action: String? = null, onAction: () -> Unit = {}) {
    Row(Modifier.fillMaxWidth().padding(bottom = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            Text(sub.uppercase(), color = Accent, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        }
        if (action != null) SmallBtn(action, BtnKind.Primary, onClick = onAction)
    }
}

@Composable
fun Tag(text: String, bg: Color, fg: Color) {
    Box(Modifier.clip(CircleShape).background(bg).padding(horizontal = 6.dp, vertical = 1.dp)) {
        Text(text, color = fg, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
    }
}

@Composable
fun Chip(text: String, badge: Int, selected: Boolean, onClick: () -> Unit) {
    val bg by animateColorAsState(if (selected) Accent else Color(0x22BFE6FF), tween(250), label = "chipbg")
    val fg by animateColorAsState(if (selected) Bg else Ice, tween(250), label = "chipfg")
    Row(
        Modifier.clip(CircleShape).background(bg).clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text, color = fg, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
        Text("$badge", color = fg, fontSize = 10.sp)
    }
}

@Composable
fun PillSelector(options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth().clip(CircleShape).background(Color(0x22BFE6FF)).padding(3.dp)) {
        options.forEachIndexed { i, o ->
            val bg by animateColorAsState(if (i == selected) Accent else Color.Transparent, tween(250), label = "pb")
            val fg by animateColorAsState(if (i == selected) Bg else Ice, tween(250), label = "pf")
            Box(
                Modifier.weight(1f).clip(CircleShape).background(bg).clickable { onSelect(i) }.padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) { Text(o, color = fg, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold) }
        }
    }
}

@Composable
fun MetricPill(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(18.dp)).background(Color(0x22BFE6FF)).padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = color, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Text(label.uppercase(), color = Ice.copy(alpha = .7f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SearchField(value: String, onChange: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value, onValueChange = onChange, singleLine = true, modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, fontSize = 13.sp, color = Ice.copy(alpha = .5f)) },
        leadingIcon = { Icon(Icons.Filled.Search, null, tint = Accent) },
        trailingIcon = {
            AnimatedVisibility(value.isNotEmpty(), enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut()) {
                IconButton(onClick = { onChange("") }) { Icon(Icons.Filled.Close, null, tint = Ice) }
            }
        },
        shape = CircleShape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Accent, unfocusedBorderColor = Color(0x44BFE6FF),
            focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = Accent,
            focusedContainerColor = Color(0x14BFE6FF), unfocusedContainerColor = Color(0x14BFE6FF)
        )
    )
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
    Box(Modifier.size(size).clip(RoundedCornerShape(12.dp)).background(Color(0x22BFE6FF))) {
        if (b != null) Image(b, null, Modifier.fillMaxSize())
    }
}

@Composable
fun GlacierSwitch(checked: Boolean, onChange: (Boolean) -> Unit) {
    Switch(
        checked = checked, onCheckedChange = onChange,
        colors = SwitchDefaults.colors(
            checkedTrackColor = Accent, checkedThumbColor = Color.White,
            uncheckedTrackColor = Color(0x33BFE6FF), uncheckedThumbColor = Ice, uncheckedBorderColor = Color.Transparent
        )
    )
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
    Box(Modifier.fillMaxWidth().statusBarsPadding().padding(top = 8.dp, start = 24.dp, end = 24.dp), contentAlignment = Alignment.TopCenter) {
        AnimatedVisibility(
            show,
            enter = slideInVertically(spring(0.6f, 300f)) { -it * 2 } + fadeIn() + scaleIn(initialScale = 0.85f),
            exit = slideOutVertically { -it * 2 } + fadeOut()
        ) {
            Row(
                Modifier.clip(CircleShape).background(Color(0xF0102455)).border(1.dp, Color(0x66BFE6FF), CircleShape)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(Modifier.size(24.dp).clip(CircleShape).background(Accent), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Check, null, tint = Bg, modifier = Modifier.size(15.dp))
                }
                Text(text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
