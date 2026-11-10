package com.glacier.profilapps

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SaranScreen(vm: MainVM) {
    LaunchedEffect(vm.apps.size) { vm.scanBlockers() }
    LazyColumn(contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { ScreenHeader("Preset Throttler", "Universal FPS Booster", "Scan") { vm.scanBlockers() } }
        item {
            GlassCard {
                Text("Pembatas FPS & Background", color = Ice, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                Text("${vm.blockers.size} layanan terdeteksi", color = Ice.copy(alpha = .65f), fontSize = 11.sp)
                Spacer(Modifier.height(10.dp))
                SmallBtn("Bekukan Semua", BtnKind.Danger) { vm.freezeAllBlockers() }
            }
        }
        if (vm.blockers.isEmpty()) {
            item {
                Text(
                    "Tidak ada layanan pembatas performa atau background terdeteksi.",
                    color = Ice.copy(alpha = .6f), fontSize = 13.sp, modifier = Modifier.fillMaxWidth().padding(24.dp)
                )
            }
        }
        items(vm.blockers, key = { it.pkg }) { b ->
            val frozen = vm.isFrozen(b.pkg)
            Row(
                Modifier.animateItem().fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Color(0x1ABFE6FF))
                    .border(1.dp, Color(0x33BFE6FF), RoundedCornerShape(22.dp)).padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppIcon(b.pkg)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(vm.name(b.pkg), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(b.pkg, color = Accent.copy(alpha = .8f), fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Tag(b.desc, Color(0x33F43F5E), Color(0xFFFDA4AF))
                        if (frozen) Tag("BEKU", Color(0x33F43F5E), Color(0xFFFDA4AF)) else Tag("AKTIF", Color(0x3310B981), Color(0xFF6EE7B7))
                    }
                }
                if (frozen) SmallBtn("Cairkan", BtnKind.Success) { vm.unfreeze(b.pkg) }
                else SmallBtn("Bekukan", BtnKind.Danger) { vm.freeze(b.pkg) }
            }
        }
    }
}
