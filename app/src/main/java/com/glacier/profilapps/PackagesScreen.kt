package com.glacier.profilapps

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PackagesScreen(vm: MainVM) {
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableIntStateOf(0) }
    var confirm by remember { mutableStateOf<AppItem?>(null) }
    val list = remember(vm.apps, query, filter) {
        vm.apps.filter { a ->
            (query.isBlank() || a.name.contains(query, true) || a.pkg.contains(query, true)) && when (filter) {
                1 -> a.isUser
                2 -> a.frozen
                3 -> !a.isUser
                else -> true
            }
        }
    }
    val user = vm.apps.count { it.isUser }
    val frozen = vm.apps.count { it.frozen }

    LazyColumn(contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 110.dp)) {
        item { HeaderCapsule(Ic.Pkg, "Glacier Freeze", "Packages Controller", "Sync") { vm.loadApps() } }
        item {
            Row(Modifier.padding(bottom = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricPill("${vm.apps.size}", "Total", TextMain, Modifier.weight(1f))
                MetricPill("$user", "User", IceDeep, Modifier.weight(1f))
                MetricPill("${vm.apps.size - user}", "Sistem", Warning, Modifier.weight(1f))
                MetricPill("$frozen", "Beku", Danger, Modifier.weight(1f))
            }
        }
        item { SearchField(query, { query = it }, "Cari nama aplikasi atau nama paket...", Modifier.padding(bottom = 14.dp)) }
        item {
            Row(
                Modifier.padding(bottom = 14.dp).horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Chip("Semua", vm.apps.size, filter == 0) { filter = 0 }
                Chip("Pengguna", user, filter == 1) { filter = 1 }
                Chip("Dibekukan", frozen, filter == 2) { filter = 2 }
                Chip("Sistem", vm.apps.size - user, filter == 3) { filter = 3 }
            }
        }
        if (filter == 2 && frozen > 0) {
            item {
                Row(
                    Modifier.padding(bottom = 12.dp).fillMaxWidth().clip(CircleShape).background(DangerLight)
                        .border(1.5.dp, Danger, CircleShape).padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    I(Ic.Shield, Danger, 16.dp)
                    Spacer(Modifier.width(6.dp))
                    Text("Aplikasi Terbekukan", color = Danger, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                    Btn("Cairkan Semua", BtnKind.Success) { vm.unfreezeAll() }
                }
            }
        }
        item {
            Box(Modifier.pitSlice(true, false).padding(10.dp, 12.dp, 10.dp, 6.dp)) {
                TableHead("Aplikasi Terdeteksi") { Text("AKSI", color = TextMain, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold) }
            }
        }
        if (list.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().pitSlice(false, true).padding(36.dp), contentAlignment = Alignment.Center) {
                    Text(
                        if (vm.loading) "Menyambungkan paket..." else "Aplikasi tidak ditemukan.",
                        color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        itemsIndexed(list, key = { _, a -> a.pkg }) { i, app ->
            val last = i == list.lastIndex
            Box(
                Modifier.animateItem().fillMaxWidth().pitSlice(false, last)
                    .padding(10.dp, 4.dp, 10.dp, if (last) 12.dp else 4.dp)
            ) { AppRow(app, app.pkg in vm.whitelist, vm) { confirm = app } }
        }
    }

    confirm?.let { a ->
        ConfirmSystemDialog(a, onConfirm = { vm.freeze(a.pkg); confirm = null }, onDismiss = { confirm = null })
    }
}

@Composable
private fun AppRow(app: AppItem, wl: Boolean, vm: MainVM, askSystem: () -> Unit) {
    CapsuleRow {
        AppIcon(app.pkg)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(app.name, color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(app.pkg, color = IceDeep, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (app.isUser) Tag("PENGGUNA", IceBorder, IceDeep) else Tag("SISTEM", WarningLight, Warning)
                if (app.frozen) Tag("BEKU", DangerLight, Danger) else Tag("AKTIF", SuccessLight, Success)
                if (wl) Tag("WHITELIST", WarningLight, Warning, Color(0xFFFCD34D))
            }
        }
        Spacer(Modifier.width(8.dp))
        Box(
            Modifier.size(32.dp).pressable { vm.toggleWhitelist(app.pkg) }.clip(CircleShape)
                .background(if (wl) WarningLight else Color.White)
                .border(1.5.dp, if (wl) Color(0xFFF59E0B) else IceBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) { I(Ic.Star, if (wl) Warning else TextMuted, 16.dp) }
        Spacer(Modifier.width(6.dp))
        if (app.frozen) Btn("Cairkan", BtnKind.Success) { vm.unfreeze(app.pkg) }
        else Btn("Bekukan", BtnKind.Danger) { if (!app.isUser) askSystem() else vm.freeze(app.pkg) }
    }
}
