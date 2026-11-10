package com.glacier.profilapps

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    val exportL = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { u ->
        if (u != null) vm.exportTo(u)
    }
    val importL = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { u ->
        if (u != null) vm.importFrom(u)
    }
    val kernel = remember { System.getProperty("os.version") ?: "Linux Kernel" }
    val device = remember { "${Build.BRAND.uppercase()} ${Build.MODEL} • Android ${Build.VERSION.RELEASE}" }
    val modes = mapOf("powersaver" to "Power Saver", "balance" to "Balance", "extreme" to "Extreme")

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        ScreenHeader("Glacier Home", "Core Ecosystem", "Refresh") { vm.refreshAll() }

        Appear(0) {
            Column {
                if (vm.rootOk == false) {
                    GlassCard(onClick = { vm.recheckRoot() }) {
                        Text("Akses root belum aktif", color = Color(0xFFFDA4AF), fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                        Text(
                            "Freeze, tuning, dan cleaner butuh root (KernelSU/Magisk). Beri izin root ke Glacier Profil Apps lalu tap di sini untuk cek ulang.",
                            color = Ice.copy(alpha = .8f), fontSize = 11.sp
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }
                Image(
                    painterResource(R.drawable.banner), null,
                    Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(RoundedCornerShape(24.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Appear(1) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GlassCard(Modifier.weight(1f)) {
                    Text("KERNEL OS", color = Accent, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                    Text(kernel, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                }
                GlassCard(Modifier.weight(1f)) {
                    Text("PERANGKAT & OS", color = Accent, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                    Text(device, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Appear(2) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ModeCard("Mode Daily", "Mode: ${modes[vm.dailyMode] ?: "Balance"}", Icons.Filled.Refresh, Modifier.weight(1f)) { showDaily = true }
                ModeCard("Game Space", "${vm.games.size} Game Terkonfigurasi", Icons.Filled.PlayArrow, Modifier.weight(1f)) { showGame = true }
            }
        }
        Spacer(Modifier.height(12.dp))
        Appear(3) {
            GlassCard(onClick = { vm.resetAll() }) {
                Text("Reset Pengaturan Sistem", color = Color(0xFFFDA4AF), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                Text("Kembalikan animasi, refresh rate, dan tuning ke default Android", color = Ice.copy(alpha = .7f), fontSize = 11.sp)
            }
        }
        Spacer(Modifier.height(12.dp))
        Appear(4) {
            GlassCard {
                Text("Cadangan & Pemulihan (JSON)", color = Accent, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                Spacer(Modifier.height(10.dp))
                BackupRow("Simpan Konfigurasi Aktif", "Cadangkan setelan ke glacier.json", "Save", BtnKind.Success) { exportL.launch("glacier.json") }
                Spacer(Modifier.height(10.dp))
                BackupRow("Pulihkan Konfigurasi", "Terapkan dari berkas glacier.json", "Import", BtnKind.Primary) {
                    importL.launch(arrayOf("application/json", "*/*"))
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Appear(5) {
            GlassCard(onClick = { showHelp = true }) {
                Text("Buku Panduan", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                Text("Cara pakai, fungsi tombol, dan konfigurasi optimal", color = Ice.copy(alpha = .7f), fontSize = 11.sp)
            }
        }
        Spacer(Modifier.height(24.dp))
    }

    if (showDaily) DailyDialog(vm) { showDaily = false }
    if (showGame) GameSpaceDialog(vm) { showGame = false }
    if (showHelp) HelpDialog { showHelp = false }
}

@Composable
private fun ModeCard(title: String, status: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    GlassCard(modifier, onClick = onClick) {
        Box(Modifier.size(34.dp).clip(CircleShape).background(Color(0x334FC3FF)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Ice, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(title, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
        Text(status, color = Accent, fontSize = 11.sp)
    }
}

@Composable
private fun BackupRow(title: String, sub: String, btn: String, kind: BtnKind, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(sub, color = Ice.copy(alpha = .65f), fontSize = 10.sp)
        }
        SmallBtn(btn, kind, onClick = onClick)
    }
}
