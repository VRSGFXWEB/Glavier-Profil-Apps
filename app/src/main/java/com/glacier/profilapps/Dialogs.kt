package com.glacier.profilapps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.math.roundToInt

@Composable
fun GlacierDialog(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val maxH = (LocalConfiguration.current.screenHeightDp * 0.85f).dp
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            Modifier.padding(16.dp).fillMaxWidth().heightIn(max = maxH)
                .clip(RoundedCornerShape(26.dp)).background(Color(0xFF0B1840))
                .border(1.5.dp, Color(0x66BFE6FF), RoundedCornerShape(26.dp))
                .verticalScroll(rememberScrollState()).padding(18.dp),
            content = content
        )
    }
}

@Composable
fun DialogTitle(t: String, d: String? = null) {
    Text(t, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
    if (d != null) Text(d, color = Ice.copy(alpha = .75f), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
    Spacer(Modifier.height(14.dp))
}

@Composable
fun DailyDialog(vm: MainVM, onDismiss: () -> Unit) {
    val items = listOf(
        Triple("powersaver", "Power Saver", "Menghemat daya dan membatasi proses latar belakang."),
        Triple("balance", "Balance (Rekomendasi)", "Seimbang antara kestabilan animasi UI dan efisiensi baterai."),
        Triple("extreme", "Extreme Daily", "Responsivitas instan tanpa batas throttling untuk penggunaan berat."),
    )
    GlacierDialog(onDismiss) {
        DialogTitle("Pilih Profil Daily", "Pilih profil manajemen daya dan frekuensi CPU untuk penggunaan harian:")
        items.forEach { (id, title, desc) ->
            val sel = vm.dailyMode == id
            GlassCard(onClick = { vm.applyDaily(id); onDismiss() }) {
                Text(title, color = if (sel) Accent else Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                Text(desc, color = Ice.copy(alpha = .7f), fontSize = 11.sp)
            }
            Spacer(Modifier.height(8.dp))
        }
        SmallBtn("Tutup", BtnKind.Primary, Modifier.align(Alignment.End), onDismiss)
    }
}

@Composable
fun HelpDialog(onDismiss: () -> Unit) {
    val rows = listOf(
        "Home" to "Memantau kernel, info perangkat, memilih profil Mode Daily, dan Game Space.",
        "Save Settings" to "Mengekspor semua pengaturan (aplikasi beku, whitelist, Game Space) ke glacier.json.",
        "Import File" to "Membaca glacier.json cadangan dan memulihkan seluruh setelan.",
        "Reset Pengaturan" to "Mengembalikan skala animasi, refresh rate, dan tuning ke default Android.",
        "Game Space" to "Menyuntikkan driver ANGLE/Vulkan, target FPS 60-144Hz, Touch Sampling dan Fast Touch per aplikasi.",
        "Whitelist (bintang)" to "Aplikasi yang di-whitelist tidak akan dimatikan oleh cleaner.",
        "Hentikan Latar Belakang" to "Menutup proses background aplikasi pihak ketiga dan memadatkan RAM.",
        "Deep Wipe Cache" to "Menghapus cache sistem, cache package, thumbnails, dan residu storage.",
        "Catatan" to "Fitur freeze, tuning, dan cleaner membutuhkan akses root (KernelSU / Magisk).",
    )
    GlacierDialog(onDismiss) {
        DialogTitle("Buku Panduan", "Fungsi sistem dan tombol pada Glacier Profil Apps:")
        rows.forEach { (k, v) ->
            Text(k, color = Accent, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
            Text(v, color = Color.White.copy(alpha = .85f), fontSize = 12.sp, modifier = Modifier.padding(bottom = 10.dp))
        }
        SmallBtn("Tutup Panduan", BtnKind.Primary, Modifier.align(Alignment.End), onDismiss)
    }
}

@Composable
fun ConfirmSystemDialog(app: AppItem, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    GlacierDialog(onDismiss) {
        DialogTitle("Peringatan Sistem")
        Text(
            "${app.name} adalah modul sistem Android. Membekukannya dapat memengaruhi kestabilan ROM. Lanjutkan?",
            color = Color.White.copy(alpha = .85f), fontSize = 13.sp
        )
        Spacer(Modifier.height(16.dp))
        Row(Modifier.align(Alignment.End), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SmallBtn("Batal", BtnKind.Primary, onClick = onDismiss)
            SmallBtn("Tetap Bekukan", BtnKind.Danger, onClick = onConfirm)
        }
    }
}

@Composable
fun GameSpaceDialog(vm: MainVM, onDismiss: () -> Unit) {
    var q by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf<String?>(null) }
    val results = remember(q, vm.apps) {
        if (q.isBlank()) emptyList()
        else vm.apps.filter { it.name.contains(q, true) || it.pkg.contains(q, true) }.take(6)
    }
    GlacierDialog(onDismiss) {
        DialogTitle("Ruang Game", "Renderer ANGLE/Vulkan, target FPS, Touch Sampling dan Fast Touch per game:")
        SearchField(q, { q = it }, "Cari & tambah game...")
        Spacer(Modifier.height(8.dp))
        results.forEach { a ->
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                AppIcon(a.pkg, 36.dp)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(a.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    Text(a.pkg, color = Ice.copy(alpha = .6f), fontSize = 10.sp, maxLines = 1)
                }
                SmallBtn("Tambah", BtnKind.Success) { vm.addGame(a.pkg); expanded = a.pkg; q = "" }
            }
        }
        Spacer(Modifier.height(8.dp))
        if (vm.games.isEmpty()) {
            Text(
                "Belum ada game. Cari di atas untuk menambahkan.", color = Ice.copy(alpha = .6f), fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 16.dp).align(Alignment.CenterHorizontally)
            )
        }
        vm.games.forEach { g ->
            GameCard(g, expanded == g.pkg, vm) { expanded = if (expanded == g.pkg) null else g.pkg }
            Spacer(Modifier.height(8.dp))
        }
        SmallBtn("Selesai", BtnKind.Primary, Modifier.align(Alignment.End), onDismiss)
    }
}

@Composable
private fun GameCard(g: GameCfg, expanded: Boolean, vm: MainVM, onToggle: () -> Unit) {
    val rot by animateFloatAsState(if (expanded) 180f else 0f, label = "rot")
    val renderers = listOf("default", "opengl", "vulkan", "angle")
    val fpsList = listOf("60", "90", "120", "144")
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color(0x14BFE6FF))
            .border(1.dp, Color(0x33BFE6FF), RoundedCornerShape(20.dp)).animateContentSize().padding(10.dp)
    ) {
        Row(Modifier.fillMaxWidth().clickable { onToggle() }, verticalAlignment = Alignment.CenterVertically) {
            AppIcon(g.pkg)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(vm.name(g.pkg), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                Text(g.pkg, color = Ice.copy(alpha = .6f), fontSize = 10.sp, maxLines = 1)
            }
            SmallBtn("Hapus", BtnKind.Danger) { vm.removeGame(g.pkg) }
            Icon(Icons.Filled.KeyboardArrowDown, null, tint = Ice, modifier = Modifier.rotate(rot))
        }
        AnimatedVisibility(expanded) {
            Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Graphics Renderer", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                PillSelector(listOf("Default", "OpenGL", "Vulkan", "ANGLE"), renderers.indexOf(g.renderer).coerceAtLeast(0)) {
                    vm.updateGame(g.copy(renderer = renderers[it]))
                }
                Text("Target Refresh Rate (FPS)", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                PillSelector(fpsList, fpsList.indexOf(g.fps).coerceAtLeast(0)) { vm.updateGame(g.copy(fps = fpsList[it])) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Speed Compile Mode", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Dexopt compiler mode speed ke package", color = Ice.copy(alpha = .6f), fontSize = 10.sp)
                    }
                    GlacierSwitch(g.speedCompile) { vm.updateGame(g.copy(speedCompile = it)) }
                }
                Row { Text("Touch Sampling Rate", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); Text("${g.touchSampling} Hz", color = Accent, fontSize = 12.sp) }
                Slider(
                    g.touchSampling.toFloat(), { vm.updateGame(g.copy(touchSampling = it.roundToInt())) }, valueRange = 20f..420f,
                    colors = SliderDefaults.colors(thumbColor = Accent, activeTrackColor = Accent, inactiveTrackColor = Color(0x33BFE6FF))
                )
                Row { Text("Fast Touch Response", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); Text("${g.fastTouch} Hz", color = Accent, fontSize = 12.sp) }
                Slider(
                    g.fastTouch.toFloat(), { vm.updateGame(g.copy(fastTouch = it.roundToInt())) }, valueRange = 60f..144f,
                    colors = SliderDefaults.colors(thumbColor = Accent, activeTrackColor = Accent, inactiveTrackColor = Color(0x33BFE6FF))
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SmallBtn("Terapkan Profil", BtnKind.Success, Modifier.weight(1f)) { vm.injectGame(g) }
                    SmallBtn("Luncurkan Game", BtnKind.Primary, Modifier.weight(1f)) { vm.launchGame(g.pkg) }
                }
            }
        }
    }
}
