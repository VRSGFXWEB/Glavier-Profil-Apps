package com.glacier.profilapps

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun CleanerScreen(vm: MainVM) {
    LaunchedEffect(Unit) { while (true) { vm.refreshRam(); delay(4000) } }
    val r = vm.ram
    val frac by animateFloatAsState(r.pct / 100f, tween(600), label = "ram")

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        ScreenHeader("Glacier Clean", "RAM Optimizer", "Live Sync") { vm.refreshAll() }
        Appear(0) {
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Status RAM Fisik", color = Ice, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    Text("${r.pct}%", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                }
                Spacer(Modifier.height(10.dp))
                Box(Modifier.fillMaxWidth().height(14.dp).clip(CircleShape).background(Color(0x33BFE6FF))) {
                    Box(Modifier.fillMaxHeight().fillMaxWidth(frac).clip(CircleShape).background(Brush.horizontalGradient(listOf(Color(0xFF7DD3FC), Accent, AccentDeep))))
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MetricPill(formatMemory(r.usedKb), "Terpakai", Color.White, Modifier.weight(1f))
                    MetricPill(formatMemory(r.availKb), "Bebas", Color(0xFF6EE7B7), Modifier.weight(1f))
                    MetricPill(formatMemory(r.totalKb), "Kapasitas", Ice, Modifier.weight(1f))
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Appear(1) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GlassCard(Modifier.weight(1f), onClick = { vm.stopBackground() }) {
                    Text("Hentikan Layanan Latar Belakang", color = Color(0xFFFDA4AF), fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                }
                GlassCard(Modifier.weight(1f), onClick = { vm.wipeCache() }) {
                    Text("Deep Wipe Cache", color = Accent, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Appear(2) {
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Smooth Launcher & UI Tweaks", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                        Text("Optimasi GPU & responsivitas animasi launcher.", color = Ice.copy(alpha = .65f), fontSize = 11.sp)
                    }
                    GlacierSwitch(vm.smooth) { vm.toggleSmooth() }
                }
            }
        }
    }
}
