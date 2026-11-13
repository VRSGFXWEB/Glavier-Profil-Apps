package com.glacier.profilapps

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SaranScreen(vm: MainVM) {
    LaunchedEffect(vm.apps.size) { vm.scanBlockers() }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp, 16.dp, 16.dp, 110.dp)) {
        HeaderCapsule(Ic.Shield, "Preset Throttler Freeze", "Universal FPS Booster", "Scan") { vm.scanBlockers() }
        PitBox {
            TableHead("Pembatas FPS & Background") { Btn("Bekukan Semua", BtnKind.Danger) { vm.freezeAllBlockers() } }
            if (vm.blockers.isEmpty()) {
                Text(
                    "Tidak ada layanan pembatas performa atau background terdeteksi!",
                    color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth().padding(24.dp)
                )
            }
            vm.blockers.forEach { b ->
                val frozen = vm.isFrozen(b.pkg)
                CapsuleRow {
                    AppIcon(b.pkg)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(vm.name(b.pkg), color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(b.pkg, color = IceDeep, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Tag(b.desc.take(18), DangerLight, Danger, Danger.copy(alpha = 0.5f))
                            if (frozen) Tag("BEKU", DangerLight, Danger) else Tag("AKTIF", SuccessLight, Success)
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    if (frozen) Btn("Cairkan", BtnKind.Success) { vm.unfreeze(b.pkg) }
                    else Btn("Bekukan", BtnKind.Danger) { vm.freeze(b.pkg) }
                }
            }
        }
    }
}
