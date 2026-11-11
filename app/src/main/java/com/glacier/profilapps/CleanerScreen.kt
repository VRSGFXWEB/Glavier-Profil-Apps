package com.glacier.profilapps

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun CleanerScreen(vm: MainVM, active: Boolean) {
    LaunchedEffect(active) { while (active) { vm.refreshRam(); delay(4000) } }
    val r = vm.ram
    val frac by animateFloatAsState(r.pct / 100f, tween(600), label = "ram")

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp, 16.dp, 16.dp, 110.dp)) {
        HeaderCapsule(Ic.Brush, "Glacier Clean", "RAM Optimizer", "Live Sync") { vm.refreshAll() }
        Appear(0) {
            Box(Modifier.padding(bottom = 12.dp)) {
                PitBox(Modifier, ) {
                    Row(Modifier.padding(horizontal = 6.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        I(Ic.Ram, IceDeep, 16.dp)
                        Spacer(Modifier.width(6.dp))
                        Text("Status RAM Fisik", color = IceDeep, fontSize = 13.6.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                        Text("${r.pct}%", color = IceDeep, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Box(
                        Modifier.padding(horizontal = 6.dp).fillMaxWidth().height(14.dp).clip(CircleShape).background(Color.White)
                            .inset(CircleShape, 2.dp).border(1.5.dp, IceBorder, CircleShape)
                    ) {
                        Box(
                            Modifier.fillMaxHeight().fillMaxWidth(frac).clip(CircleShape)
                                .background(Brush.horizontalGradient(listOf(IceRim, IceGlow, IcePrimary)))
                        )
                    }
                    Row(Modifier.padding(horizontal = 6.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        StatPill(formatMemory(r.usedKb), "Terpakai", TextMain, Modifier.weight(1f))
                        StatPill(formatMemory(r.availKb), "Bebas", Success, Modifier.weight(1f))
                        StatPill(formatMemory(r.totalKb), "Kapasitas", TextMain, Modifier.weight(1f))
                    }
                }
            }
        }
        Appear(1) {
            Row(Modifier.padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CleanBtn(Ic.Zap, Danger, "Berhentikan Layanan Latar Belakang", Modifier.weight(1f)) { vm.stopBackground() }
                CleanBtn(Ic.Brush, IceDeep, "Deep Wipe Cache", Modifier.weight(1f)) { vm.wipeCache() }
            }
        }
        Appear(2) {
            val shape = RoundedCornerShape(20.dp)
            Row(
                Modifier.fillMaxWidth().shadow(3.dp, shape, ambientColor = ShadowIce, spotColor = ShadowIce).clip(shape)
                    .background(Brush.linearGradient(listOf(Color.White, IceSurface))).border(1.5.dp, IceBorder, shape)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Smooth Launcher & UI Tweaks", color = TextMain, fontSize = 12.8.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Suntikkan optimasi GPU & responsivitas animasi launcher.", color = TextMuted, fontSize = 10.4.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.width(12.dp))
                IceSwitch(vm.smooth) { vm.toggleSmooth() }
            }
        }
    }
}

@Composable
private fun StatPill(value: String, label: String, color: Color, modifier: Modifier) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier.shadow(2.dp, shape, ambientColor = ShadowIce, spotColor = ShadowIce).clip(shape).background(Color.White)
            .border(1.5.dp, IceBorder, shape).padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = color, fontSize = 13.8.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
        Text(label.uppercase(), color = TextMuted, fontSize = 9.6.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
    }
}

@Composable
private fun CleanBtn(icon: ImageVector, tint: Color, text: String, modifier: Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier.pressable(onClick).shadow(4.dp, shape, ambientColor = ShadowIce, spotColor = ShadowIce).clip(shape)
            .background(Brush.linearGradient(listOf(Color.White, IceSurface))).border(1.5.dp, IceBorder, shape)
            .padding(horizontal = 10.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        I(icon, tint, 20.dp)
        Text(text, color = TextMain, fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
    }
}
