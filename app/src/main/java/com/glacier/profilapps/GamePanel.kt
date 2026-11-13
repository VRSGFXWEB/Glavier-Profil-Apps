package com.glacier.profilapps

import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Environment
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private val PanelDark = Color(0xFF0A0F1C)
private val PanelCard = Color(0xFF131A2C)
private val PanelBorderC = Color(0x33FFFFFF)
private val PanelAccent = Color(0xFFE11D48)
private val PanelText = Color(0xFFE5E7EB)
private val PanelMuted = Color(0xFF8A94A6)

data class ToolItem(val icon: ImageVector, val label: String, val action: (CoroutineScope, Context) -> Unit)

/** Panel full-screen ala Game Tools, dipasang lewat overlay WindowManager. */
@Composable
fun GamePanel(ctx: Context, onClosed: () -> Unit, openApp: () -> Unit) {
    val scope = rememberCoroutineScope()
    var brightness by remember { mutableFloatStateOf(readBrightness(ctx)) }
    var recording by remember { mutableStateOf(false) }
    var showGames by remember { mutableStateOf(false) }
    val gamePkg = OverlayState.gamePkg

    Box(Modifier.fillMaxSize()) {
        // backdrop setengah transparan, tap di luar kartu = tidak menutup (overlay non-focusable area game tetap kelihatan)
        Box(Modifier.fillMaxSize().background(Color(0xB3000000)))

        Column(Modifier.fillMaxSize().padding(14.dp)) {
            // ---------- top bar ----------
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(PanelCard)
                    .border(1.dp, PanelBorderC, RoundedCornerShape(24.dp)).padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TopIcon(Ic.Home, "Home") { openApp() }
                Spacer(Modifier.width(8.dp))
                TopIcon(Ic.Save, "Notes") { }
                Spacer(Modifier.width(8.dp))
                TopIcon(Ic.Gamepad, "Game") { showGames = true }
                Spacer(Modifier.width(8.dp))
                TopIcon(Ic.Cpu, "Setting") { }
                Spacer(Modifier.weight(1f))
                gamePkg?.let {
                    Text(it.substringAfterLast('.'), color = PanelMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    Spacer(Modifier.width(10.dp))
                }
                Box(
                    Modifier.size(38.dp).clip(CircleShape).background(Color(0x22FFFFFF)).clickable { onClosed() },
                    contentAlignment = Alignment.Center
                ) { I(Ic.Chevron, PanelText, 18.dp, Modifier.rotate(90f)) }
            }

            Spacer(Modifier.height(10.dp))

            Row(Modifier.weight(1f).fillMaxWidth()) {
                // ---------- left: tools ----------
                LazyColumn(Modifier.width(132.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        ToolBtn(Ic.Zap, "Boost RAM") { scope.launch { boostNow(ctx) } }
                    }
                    item {
                        ToolBtn(Ic.Camera, "Screenshot") { scope.launch { takeScreenshot(ctx) } }
                    }
                    item {
                        ToolBtn(if (recording) Ic.Record else Ic.Record, if (recording) "Stop Record" else "Record") {
                            scope.launch { recording = toggleRecord(ctx, recording) }
                        }
                    }
                    item {
                        ToolBtn(Ic.Brush, "Clear Cache") { scope.launch { try { Sh.run("pm trim-caches 999G") } catch (e: Exception) { } } }
                    }
                    item {
                        ToolBtn(Ic.Gamepad, "Pilih Game") { showGames = true }
                    }
                }

                Spacer(Modifier.width(10.dp))

                // ---------- center: cpu / gpu gauges ----------
                Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.Top) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Gauge("CPU", readCpuFreq(), scope)
                        Gauge("GPU", readGpuFreq(), scope)
                    }
                    Spacer(Modifier.weight(1f))
                    // ---------- brightness slider (vertikal) ----------
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        BrightnessBar(brightness) { v ->
                            brightness = v
                            scope.launch(Dispatchers.IO) { setBrightness(ctx, v) }
                        }
                    }
                }

                Spacer(Modifier.width(10.dp))

                // ---------- right: quick toggles ----------
                LazyColumn(Modifier.width(76.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { QuickToggle(Ic.Wifi, "WiFi", isWifiOn(ctx)) { toggleWifi(ctx) } }
                    item { QuickToggle(Ic.Dnd, "DND", false) { scope.launch { toggleDnd(ctx) } } }
                    item { QuickToggle(Ic.Speed, "60Hz", true) { scope.launch { toggleRefresh() } } }
                    item { QuickToggle(Ic.Lock, "Rotasi", !isAutoRotateOn(ctx)) { toggleRotation(ctx) } }
                    item { QuickToggle(Ic.Mute, "Mute", isMuted(ctx)) { toggleMute(ctx) } }
                }
            }
        }
    }

    if (showGames) GamePickerDialog(ctx, onDismiss = { showGames = false }, onPick = { pkg ->
        showGames = false
        OverlayState.gamePkg = pkg
        ctx.packageManager.getLaunchIntentForPackage(pkg)?.let {
            it.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            ctx.startActivity(it)
        }
    })
}

@Composable
private fun TopIcon(icon: ImageVector, label: String, onClick: () -> Unit) {
    Box(
        Modifier.size(38.dp).clip(CircleShape).background(Color(0x22FFFFFF)).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) { I(icon, PanelText, 17.dp) }
}

@Composable
private fun ToolBtn(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(PanelCard)
            .border(1.dp, PanelBorderC, RoundedCornerShape(18.dp)).clickable { onClick() }.padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            Modifier.size(34.dp).clip(CircleShape).background(Color(0x22FFFFFF)),
            contentAlignment = Alignment.Center
        ) { I(icon, Color(0xFF38BDF8), 16.dp) }
        Text(label, color = PanelText, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 2)
    }
}

@Composable
private fun QuickToggle(icon: ImageVector, label: String, active: Boolean, onClick: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(if (active) PanelAccent else Color(0x22FFFFFF)).clickable { onClick() }.padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        I(icon, Color.White, 16.dp)
        Text(label, color = Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun Gauge(label: String, value: String, scope: CoroutineScope) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(84.dp).clip(CircleShape).background(Color(0x14FFFFFF))
                .border(3.dp, Brush.sweepGradient(listOf(PanelAccent, Color(0x22FFFFFF), PanelAccent)), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(value, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                Text(label, color = PanelAccent, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun BrightnessBar(value: Float, onChange: (Float) -> Unit) {
    val height = 160.dp
    Box(
        Modifier.width(44.dp).height(height).clip(RoundedCornerShape(22.dp)).background(Color(0x1AFFFFFF))
            .border(1.dp, PanelBorderC, RoundedCornerShape(22.dp))
            .pointerInput(Unit) {
                detectTapGestures { }
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    var y = down.position.y
                    onChange((1f - (y / size.height)).coerceIn(0f, 1f))
                    do {
                        val ev = awaitPointerEvent()
                        ev.changes.firstOrNull()?.let {
                            y = it.position.y
                            onChange((1f - (y / size.height)).coerceIn(0f, 1f))
                            it.consume()
                        }
                    } while (ev.changes.any { it.pressed })
                }
            }
    ) {
        Box(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().fillMaxHeight(value.coerceIn(0.04f, 1f))
                .clip(RoundedCornerShape(22.dp)).background(Brush.verticalGradient(listOf(Color(0xFF38BDF8), Color(0xFF0284C7))))
        )
        I(Ic.Sun, Color.White, 16.dp, Modifier.align(Alignment.TopCenter).padding(top = 8.dp))
    }
}

@Composable
private fun GamePickerDialog(ctx: Context, onDismiss: () -> Unit, onPick: (String) -> Unit) {
    val apps = remember {
        ctx.packageManager.getInstalledApplications(0)
            .filter { ctx.packageManager.getLaunchIntentForPackage(it.packageName) != null }
            .sortedBy { ctx.packageManager.getApplicationLabel(it).toString().lowercase() }
    }
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxHeight(0.8f).fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(PanelDark)
                .border(1.dp, PanelBorderC, RoundedCornerShape(24.dp)).padding(14.dp)
        ) {
            Text("Import & Mainkan Game", color = PanelText, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(10.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(apps, key = { it.packageName }) { a ->
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0x14FFFFFF))
                            .clickable { onPick(a.packageName) }.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            ctx.packageManager.getApplicationLabel(a).toString(), color = PanelText,
                            fontSize = 12.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, modifier = Modifier.weight(1f)
                        )
                        I(Ic.Play, Color(0xFF38BDF8), 16.dp)
                    }
                }
            }
        }
    }
}

// ---------------- actions ----------------

private fun readBrightness(ctx: Context): Float = try {
    Settings.System.getInt(ctx.contentResolver, Settings.System.SCREEN_BRIGHTNESS) / 255f
} catch (e: Exception) { 0.5f }

private suspend fun setBrightness(ctx: Context, v: Float) {
    val value = (v * 255).toInt().coerceIn(1, 255)
    try {
        Settings.System.putInt(ctx.contentResolver, Settings.System.SCREEN_BRIGHTNESS, value)
    } catch (e: Exception) {
        try { Sh.run("settings put system screen_brightness $value") } catch (ex: Exception) { }
    }
}

private fun isWifiOn(ctx: Context): Boolean = try {
    (ctx.getSystemService(Context.WIFI_SERVICE) as WifiManager).isWifiEnabled
} catch (e: Exception) { false }

private fun toggleWifi(ctx: Context) {
    try {
        val on = isWifiOn(ctx)
        ctx.startActivity(
            android.content.Intent(Settings.Panel.ACTION_WIFI).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    } catch (e: Exception) { }
}

private suspend fun toggleDnd(ctx: Context) {
    try { Sh.run("cmd notification set_dnd on") } catch (e: Exception) { }
}

private suspend fun toggleRefresh() { /* placeholder: 60/90/120 cycle via Shizuku/root */ }

private fun isAutoRotateOn(ctx: Context): Boolean = try {
    Settings.System.getInt(ctx.contentResolver, Settings.System.ACCELEROMETER_ROTATION) == 1
} catch (e: Exception) { true }

private fun toggleRotation(ctx: Context) {
    try {
        val cur = isAutoRotateOn(ctx)
        Settings.System.putInt(ctx.contentResolver, Settings.System.ACCELEROMETER_ROTATION, if (cur) 0 else 1)
    } catch (e: Exception) { }
}

private fun isMuted(ctx: Context): Boolean = try {
    (ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager).ringerMode == AudioManager.RINGER_MODE_SILENT
} catch (e: Exception) { false }

private fun toggleMute(ctx: Context) {
    try {
        val am = ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        am.ringerMode = if (isMuted(ctx)) AudioManager.RINGER_MODE_NORMAL else AudioManager.RINGER_MODE_SILENT
    } catch (e: Exception) { }
}

private fun readCpuFreq(): String = try {
    val khz = File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq").readText().trim().toLong()
    String.format("%.2f", khz / 1_000_000f)
} catch (e: Exception) { "--" }

private fun readGpuFreq(): String = try {
    val paths = listOf(
        "/sys/class/kgsl/kgsl-3d0/gpuclk",
        "/sys/class/devfreq/gpufreq/cur_freq",
        "/sys/kernel/gpu/gpu_clock"
    )
    val hz = paths.firstNotNullOfOrNull { p -> try { File(p).readText().trim().toLong() } catch (e: Exception) { null } }
    if (hz != null) "${hz / 1_000_000}" else "--"
} catch (e: Exception) { "--" }

private suspend fun boostNow(ctx: Context) {
    try {
        val pkgs = ctx.packageManager.getInstalledApplications(0)
            .filter { (it.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0 && it.packageName != ctx.packageName }
            .joinToString("") { "am force-stop ${it.packageName}; " }
        Sh.run("${pkgs}echo 3 > /proc/sys/vm/drop_caches 2>/dev/null; sync")
    } catch (e: Exception) { }
}

private fun outputDir(sub: String): File {
    val d = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "GlacierPanel/$sub")
    if (!d.exists()) d.mkdirs()
    return d
}

private suspend fun takeScreenshot(ctx: Context) {
    val name = "SS_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.png"
    val path = File(outputDir("Screenshots"), name).absolutePath
    try {
        Sh.run("screencap -p '$path'")
    } catch (e: Exception) { }
}

private var recordProc: Process? = null

private suspend fun toggleRecord(ctx: Context, currentlyRecording: Boolean): Boolean {
    return if (!currentlyRecording) {
        val name = "REC_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.mp4"
        val path = File(outputDir("Recordings"), name).absolutePath
        try {
            Sh.run("screenrecord '$path' & disown")
        } catch (e: Exception) { }
        true
    } else {
        try { Sh.run("pkill -INT screenrecord") } catch (e: Exception) { }
        false
    }
}
