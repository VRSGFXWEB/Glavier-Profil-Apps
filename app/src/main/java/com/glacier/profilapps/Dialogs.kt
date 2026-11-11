package com.glacier.profilapps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import kotlin.math.roundToInt

@Composable
fun GlacierDialog(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val maxH = (LocalConfiguration.current.screenHeightDp * 0.85f).dp
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        val view = LocalView.current
        SideEffect { (view.parent as? DialogWindowProvider)?.window?.setDimAmount(0.45f) }
        val shape = RoundedCornerShape(26.dp)
        Column(
            Modifier.padding(20.dp).fillMaxWidth().heightIn(max = maxH)
                .shadow(20.dp, shape, ambientColor = ShadowIce, spotColor = ShadowIce).clip(shape)
                .background(Brush.linearGradient(listOf(Color(0xFAFFFFFF), Color(0xF2F0F9FF))))
                .inset(shape, 5.dp).border(2.dp, IcePrimary, shape)
                .verticalScroll(rememberScrollState()).padding(20.dp),
            content = content
        )
    }
}

@Composable
fun ModalHead(icon: ImageVector, title: String, tint: Color = IceDeep, desc: String? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        I(icon, tint, 20.dp)
        Text(title, color = tint, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
    }
    if (desc != null) Text(desc, color = TextMuted, fontSize = 12.5.sp, lineHeight = 17.sp, modifier = Modifier.padding(top = 8.dp))
    Spacer(Modifier.height(14.dp))
}

@Composable
fun CancelBtn(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier.pressable(onClick).clip(CircleShape).background(Color(0xFFE2E8F0)).padding(vertical = 10.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) { Text(text, color = Color(0xFF334155), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold) }
}

@Composable
private fun CleanPill(onClick: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        Modifier.fillMaxWidth().pressable(onClick).shadow(3.dp, shape, ambientColor = ShadowIce, spotColor = ShadowIce).clip(shape)
            .background(Brush.linearGradient(listOf(Color.White, IceSurface))).border(1.5.dp, IceBorder, shape).padding(12.dp),
        content = content
    )
}

@Composable
fun DailyDialog(vm: MainVM, onDismiss: () -> Unit) {
    val items = listOf(
        Triple("powersaver", "Power Saver" to Success, "Menghemat konsumsi daya dan membatasi proses latar belakang."),
        Triple("balance", "Balance (Rekomendasi)" to IceDeep, "Keseimbangan antara kestabilan animasi UI dan efisiensi baterai."),
        Triple("extreme", "Extreme Daily" to Danger, "Responsivitas instan tanpa batas throttling untuk penggunaan berat."),
    )
    GlacierDialog(onDismiss) {
        ModalHead(Ic.Clock, "Pilih Profil Daily", IceDeep, "Pilih profil manajemen daya dan frekuensi CPU untuk penggunaan harian:")
        items.forEach { (id, t, desc) ->
            CleanPill({ vm.applyDaily(id); onDismiss() }) {
                Text(t.first, color = t.second, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold)
                Text(desc, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.height(6.dp))
        CancelBtn("Tutup", Modifier.fillMaxWidth(), onDismiss)
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
        "Whitelist" to "Ikon bintang. Aplikasi yang di-whitelist tidak akan dimatikan oleh cleaner.",
        "Hentikan Latar Belakang" to "Menutup proses background aplikasi pihak ketiga dan memadatkan RAM.",
        "Deep Wipe Cache" to "Menghapus cache sistem, cache package, thumbnails, dan residu storage.",
        "Catatan" to "Fitur freeze, tuning, dan cleaner membutuhkan akses root (KernelSU / Magisk).",
    )
    GlacierDialog(onDismiss) {
        ModalHead(Ic.Book, "Buku Panduan Modul", IceDeep, "Panduan fungsi sistem dan tombol pada Glacier Freeze Apps:")
        rows.forEach { (k, v) ->
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Text(k, color = IceDeep, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(0.38f))
                Text(v, color = TextMain, fontSize = 11.sp, modifier = Modifier.weight(0.62f))
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(IceBorder))
        }
        Spacer(Modifier.height(14.dp))
        Btn("Tutup Panduan", BtnKind.Primary, Modifier.fillMaxWidth(), onDismiss)
    }
}

@Composable
fun ConfirmSystemDialog(app: AppItem, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    GlacierDialog(onDismiss) {
        ModalHead(Ic.Alert, "Peringatan Sistem", Warning)
        Text(
            "Paket ${app.name} adalah modul sistem Android. Membekukannya dapat memengaruhi kestabilan ROM. Lanjutkan?",
            color = TextMuted, fontSize = 12.5.sp, lineHeight = 17.sp
        )
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CancelBtn("Batal", Modifier.weight(1f), onDismiss)
            Btn("Tetap Bekukan", BtnKind.Danger, Modifier.weight(1f), onConfirm)
        }
    }
}

@Composable
fun GameSpaceDialog(vm: MainVM, onDismiss: () -> Unit) {
    var q by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf<String?>(null) }
    val results = remember(q, vm.apps) {
        if (q.isBlank()) emptyList() else vm.apps.filter { it.name.contains(q, true) || it.pkg.contains(q, true) }.take(6)
    }
    GlacierDialog(onDismiss) {
        ModalHead(Ic.Gamepad, "Ruang Game (Game Space)", IceDeep, "Konfigurasi renderer ANGLE, Vulkan, target FPS, Touch Sampling, dan Fast Touch per-game:")
        SearchField(q, { q = it }, "Cari & tambah game dari package...")
        Spacer(Modifier.height(8.dp))
        results.forEach { a ->
            CapsuleRow(Modifier.padding(vertical = 3.dp), 54.dp) {
                AppIcon(a.pkg, 34.dp)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(a.name, color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                    Text(a.pkg, color = IceDeep, fontSize = 9.5.sp, maxLines = 1)
                }
                Btn("Tambah", BtnKind.Success) { vm.addGame(a.pkg); expanded = a.pkg; q = "" }
            }
        }
        Spacer(Modifier.height(8.dp))
        if (vm.games.isEmpty()) {
            Text(
                "Belum ada game ditambahkan. Cari di atas untuk menambahkan.", color = TextMuted, fontSize = 12.sp,
                fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 20.dp).align(Alignment.CenterHorizontally)
            )
        }
        vm.games.forEach { g ->
            GameCard(g, expanded == g.pkg, vm) { expanded = if (expanded == g.pkg) null else g.pkg }
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.height(6.dp))
        CancelBtn("Selesai", Modifier.fillMaxWidth(), onDismiss)
    }
}

@Composable
private fun GameCard(g: GameCfg, expanded: Boolean, vm: MainVM, onToggle: () -> Unit) {
    val rot by animateFloatAsState(if (expanded) 180f else 0f, label = "rot")
    val renderers = listOf("default", "opengl", "vulkan", "angle")
    val fpsList = listOf("60", "90", "120", "144")
    val shape = RoundedCornerShape(20.dp)
    val sliderColors = SliderDefaults.colors(
        thumbColor = IcePrimary, activeTrackColor = IcePrimary, inactiveTrackColor = IceLight
    )
    Column(
        Modifier.fillMaxWidth().clip(shape).background(Brush.verticalGradient(listOf(Color.White, Color(0xFFF4FAFF))))
            .inset(shape, 2.dp).border(1.5.dp, IceBorder, shape).animateContentSize().padding(10.dp)
    ) {
        Row(Modifier.fillMaxWidth().clickable { onToggle() }, verticalAlignment = Alignment.CenterVertically) {
            AppIcon(g.pkg)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(vm.name(g.pkg), color = TextMain, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                Text(g.pkg, color = IceDeep, fontSize = 9.5.sp, maxLines = 1)
            }
            Btn("Hapus", BtnKind.Danger) { vm.removeGame(g.pkg) }
            Spacer(Modifier.width(6.dp))
            I(Ic.Chevron, IceDeep, 16.dp, Modifier.rotate(90f + rot))
        }
        AnimatedVisibility(expanded) {
            Column(
                Modifier.padding(top = 10.dp).fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(IceSurface)
                    .border(1.5.dp, IceBorder, RoundedCornerShape(20.dp)).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Graphics Renderer:", color = IceDeep, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                SlidingSelector(listOf("Default", "OpenGL", "Vulkan", "ANGLE"), renderers.indexOf(g.renderer).coerceAtLeast(0)) {
                    vm.updateGame(g.copy(renderer = renderers[it]))
                }
                Text("Target Refresh Rate (FPS):", color = IceDeep, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                SlidingSelector(fpsList, fpsList.indexOf(g.fps).coerceAtLeast(0)) { vm.updateGame(g.copy(fps = fpsList[it])) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Speed Compile Mode", color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                        Text("Suntikkan dexopt compiler mode speed ke package", color = TextMuted, fontSize = 10.sp)
                    }
                    IceSwitch(g.speedCompile) { vm.updateGame(g.copy(speedCompile = it)) }
                }
                Row {
                    Text("Touch Sampling Rate", color = TextMain, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                    Text("${g.touchSampling} Hz", color = TextMain, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                }
                Slider(g.touchSampling.toFloat(), { vm.updateGame(g.copy(touchSampling = it.roundToInt())) }, valueRange = 20f..420f, colors = sliderColors)
                Row {
                    Text("Fast Touch Response", color = TextMain, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                    Text("${g.fastTouch} Hz", color = TextMain, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                }
                Slider(g.fastTouch.toFloat(), { vm.updateGame(g.copy(fastTouch = it.roundToInt())) }, valueRange = 60f..144f, colors = sliderColors)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Btn("Terapkan Profil", BtnKind.Success, Modifier.weight(1f)) { vm.injectGame(g) }
                    Btn("Luncurkan Game", BtnKind.Primary, Modifier.weight(1f)) { vm.launchGame(g.pkg) }
                }
            }
        }
    }
}
