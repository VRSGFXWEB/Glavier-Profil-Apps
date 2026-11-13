import android.content.Intent
import android.net.Uri
import android.provider.Settings
package com.glacier.profilapps

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(vm: MainVM) {
    var showDaily by remember { mutableStateOf(false) }
    var showGame by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }
    val exportL = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { u -> if (u != null) vm.exportTo(u) }
    val importL = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { u -> if (u != null) vm.importFrom(u) }
    val kernel = remember { System.getProperty("os.version") ?: "Linux Kernel" }
    val device = remember { "${Build.BRAND.uppercase()} ${Build.MODEL} • Android ${Build.VERSION.RELEASE}" }
    val modes = mapOf("powersaver" to "Power Saver", "balance" to "Balance", "extreme" to "Extreme")

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp, 16.dp, 16.dp, 110.dp)) {
        HeaderCapsule(Ic.Home, "Glacier Home", "Core Ecosystem", "Refresh") { vm.refreshAll() }

        AccessCard(vm)
        GamePanelCard(vm)

        Appear(0) {
            val shape = RoundedCornerShape(26.dp)
            Box(
                Modifier.fillMaxWidth().padding(bottom = 14.dp).aspectRatio(16f / 9f)
                    .shadow(12.dp, shape, ambientColor = ShadowIce, spotColor = ShadowIce).clip(shape)
                    .border(2.dp, WhiteRim, shape)
            ) {
                Image(painterResource(R.drawable.banner), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            }
        }
        Appear(1) {
            Row(Modifier.padding(bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GlassBox(Modifier.weight(1f)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                        I(Ic.Cpu, IcePrimary, 14.dp)
                        Text("KERNEL OS", color = IcePrimary, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Text(kernel, color = TextMain, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 15.sp)
                }
                GlassBox(Modifier.weight(1f)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                        I(Ic.Device, IcePrimary, 14.dp)
                        Text("PERANGKAT & OS", color = IcePrimary, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                    }
                    Text(device, color = TextMain, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 15.sp)
                }
            }
        }
        Appear(2) {
            Row(Modifier.padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ModeCard("Mode Daily", "Mode: ${modes[vm.dailyMode] ?: "Balance"}", Ic.Clock, Modifier.weight(1f)) { showDaily = true }
                ModeCard("Game Space", "${vm.games.size} Game Terkonfigurasi", Ic.Gamepad, Modifier.weight(1f)) { showGame = true }
            }
        }
        Appear(3) {
            Box(Modifier.padding(bottom = 12.dp)) {
                PillButton({ vm.resetAll() }, Danger, Color(0xFFFECACA)) {
                    Box(
                        Modifier.size(32.dp).shadow(3.dp, CircleShape, ambientColor = Danger, spotColor = Danger).clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color(0xFFF43F5E), Danger))),
                        contentAlignment = Alignment.Center
                    ) { I(Ic.Reset, Color.White, 16.dp) }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Reset Pengaturan Sistem", color = Danger, fontSize = 12.8.sp, fontWeight = FontWeight.ExtraBold)
                        Text("Kembalikan animasi, refresh rate, & tuning ke default Android", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                    I(Ic.Chevron, Danger, 16.dp)
                }
            }
        }
        Appear(4) {
            Box(Modifier.padding(bottom = 12.dp)) {
                PitBox {
                    TableHead("Cadangan & Pemulihan (JSON)") { Text("glacier.json", color = TextMain, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold) }
                    BackupRow(Ic.Save, "Simpan Konfigurasi Aktif", "Cadangkan seluruh setelan ke glacier.json", "Save Settings", BtnKind.Success) { exportL.launch("glacier.json") }
                    BackupRow(Ic.Upload, "Pulihkan Konfigurasi", "Terapkan dari berkas glacier.json", "Import File", BtnKind.Primary) {
                        importL.launch(arrayOf("application/json", "*/*"))
                    }
                }
            }
        }
        Appear(5) {
            PillButton({ showHelp = true }, IcePrimary, IcePrimary) {
                Box(
                    Modifier.size(32.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Color(0xFF0EA5E9), IcePrimary))),
                    contentAlignment = Alignment.Center
                ) { I(Ic.Book, Color.White, 16.dp) }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("Buku Panduan Modul", color = TextMain, fontSize = 12.8.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Cara pakai, fungsi tombol, & konfigurasi optimal", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                }
                I(Ic.Chevron, IceDeep, 16.dp)
            }
        }
    }

    if (showDaily) DailyDialog(vm) { showDaily = false }
    if (showGame) GameSpaceDialog(vm) { showGame = false }
    if (showHelp) HelpDialog { showHelp = false }
}

@Composable
private fun ModeCard(title: String, status: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    ActionCard(modifier, onClick) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(34.dp).clip(CircleShape).background(Brush.linearGradient(listOf(IceLight, IceBorder)))
                    .border(1.dp, IcePrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) { I(icon, IceDeep, 16.dp) }
            Spacer(Modifier.weight(1f))
            I(Ic.Chevron, IceDeep, 16.dp)
        }
        Column {
            Text(title, color = TextMain, fontSize = 13.6.sp, fontWeight = FontWeight.ExtraBold)
            Text(status, color = IceDeep, fontSize = 10.4.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun BackupRow(icon: ImageVector, title: String, sub: String, btn: String, kind: BtnKind, onClick: () -> Unit) {
    CapsuleRow(height = 56.dp) {
        Box(
            Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(IceLight).border(1.5.dp, IceBorder, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) { I(icon, IceDeep, 16.dp) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
            Text(sub, color = IceDeep, fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
        Spacer(Modifier.width(8.dp))
        Btn(btn, kind, onClick = onClick)
    }
}

@Composable
private fun AccessCard(vm: MainVM) {
    val prefs = listOf("auto", "root", "shizuku")
    val ok = vm.mode != Sh.Mode.NONE
    val label = when (vm.mode) {
        Sh.Mode.ROOT -> "AKTIF: ROOT"
        Sh.Mode.SHIZUKU -> if (Sh.shizukuUid() == 0) "AKTIF: SHIZUKU (ROOT)" else "AKTIF: SHIZUKU (ADB)"
        Sh.Mode.NONE -> "BELUM ADA AKSES"
    }
    Box(Modifier.padding(bottom = 14.dp)) {
        GlassBox(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                I(if (ok) Ic.Shield else Ic.Alert, if (ok) Success else Danger, 16.dp)
                Text("MODE AKSES", color = IcePrimary, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                Tag(label, if (ok) SuccessLight else DangerLight, if (ok) Success else Danger)
            }
            Spacer(Modifier.height(6.dp))
            SlidingSelector(listOf("Auto", "Root", "Shizuku"), prefs.indexOf(vm.accessPref).coerceAtLeast(0)) { vm.setAccessPref(prefs[it]) }
            if (!ok) {
                val msg = when {
                    !vm.shizukuRunning -> "Belum ada root dan Shizuku belum berjalan. Buka aplikasi Shizuku lalu Start (Wireless debugging atau root), kemudian tap Cek Ulang."
                    !vm.shizukuGranted -> "Shizuku sudah berjalan, tapi izin untuk aplikasi ini belum diberikan."
                    else -> "Mode yang dipilih belum tersedia. Ganti ke Auto atau cek ulang."
                }
                Spacer(Modifier.height(4.dp))
                Text(msg, color = TextMuted, fontSize = 10.5.sp, lineHeight = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (vm.shizukuRunning && !vm.shizukuGranted) Btn("Beri Izin Shizuku", BtnKind.Success, Modifier.weight(1f)) { vm.requestShizuku() }
                    Btn("Cek Ulang", BtnKind.Primary, Modifier.weight(1f)) { vm.recheckAccess() }
                }
            } else if (vm.mode == Sh.Mode.SHIZUKU && Sh.shizukuUid() != 0) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Mode ADB: freeze, tuning, dan cleaner jalan. Drop cache RAM dan setprop persist tidak tersedia tanpa root.",
                    color = TextMuted, fontSize = 10.5.sp, lineHeight = 14.sp, fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun GamePanelCard(vm: MainVM) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val overlayL = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) {
        if (Settings.canDrawOverlays(ctx)) OverlayService.start(ctx, null)
    }
    Box(Modifier.padding(bottom = 14.dp)) {
        PillButton(
            {
                if (Settings.canDrawOverlays(ctx)) {
                    if (OverlayState.running) OverlayService.stop(ctx) else OverlayService.start(ctx, null)
                } else {
                    overlayL.launch(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${ctx.packageName}")))
                }
            },
            IcePrimary, IcePrimary
        ) {
            Box(
                Modifier.size(32.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Color(0xFF0EA5E9), IcePrimary))),
                contentAlignment = Alignment.Center
            ) { I(Ic.Gamepad, Color.White, 16.dp) }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("Game Panel", color = TextMain, fontSize = 12.8.sp, fontWeight = FontWeight.ExtraBold)
                Text(
                    if (OverlayState.running) "Aktif - bubble mengambang di layar" else "Bubble mengambang: boost, record, SS, brightness",
                    color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold
                )
            }
            I(Ic.Chevron, IceDeep, 16.dp)
        }
    }
}
