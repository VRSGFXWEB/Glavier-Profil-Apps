package com.glacier.profilapps

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
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

private val TagUser = Color(0x334FC3FF)
private val TagSys = Color(0x33F59E0B)
private val TagFrz = Color(0x33F43F5E)
private val TagAct = Color(0x3310B981)
private val TagWht = Color(0x44FACC15)

@Composable
fun PackagesScreen(vm: MainVM) {
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableIntStateOf(0) }
    var confirm by remember { mutableStateOf<AppItem?>(null) }
    val list = remember(vm.apps, query, filter) {
        vm.apps.filter { a ->
            (query.isBlank() || a.name.contains(query, true) || a.pkg.contains(query, true)) && when (filter) {
                1 -> a.isUser; 2 -> a.frozen; 3 -> !a.isUser; else -> true
            }
        }
    }
    val user = vm.apps.count { it.isUser }
    val frozen = vm.apps.count { it.frozen }

    LazyColumn(
        contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { ScreenHeader("Glacier Freeze", "Packages Controller", "Sync") { vm.loadApps() } }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricPill("${vm.apps.size}", "Total", Color.White, Modifier.weight(1f))
                MetricPill("$user", "User", Accent, Modifier.weight(1f))
                MetricPill("${vm.apps.size - user}", "Sistem", Color(0xFFFCD34D), Modifier.weight(1f))
                MetricPill("$frozen", "Beku", Color(0xFFFDA4AF), Modifier.weight(1f))
            }
        }
        item { SearchField(query, { query = it }, "Cari nama aplikasi atau paket...") }
        item {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Chip("Semua", vm.apps.size, filter == 0) { filter = 0 }
                Chip("Pengguna", user, filter == 1) { filter = 1 }
                Chip("Dibekukan", frozen, filter == 2) { filter = 2 }
                Chip("Sistem", vm.apps.size - user, filter == 3) { filter = 3 }
            }
        }
        if (filter == 2 && frozen > 0) {
            item {
                Row(
                    Modifier.fillMaxWidth().clip(CircleShape).background(Color(0x33F43F5E)).padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Aplikasi Terbekukan", color = Color(0xFFFDA4AF), fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    SmallBtn("Cairkan Semua", BtnKind.Success) { vm.unfreezeAll() }
                }
            }
        }
        if (list.isEmpty()) {
            item {
                Text(
                    if (vm.loading) "Memuat paket..." else "Aplikasi tidak ditemukan.",
                    color = Ice.copy(alpha = .6f), modifier = Modifier.fillMaxWidth().padding(36.dp), fontSize = 13.sp
                )
            }
        }
        items(list, key = { it.pkg }) { app ->
            AppRow(app, app.pkg in vm.whitelist, vm, Modifier.animateItem()) { confirm = app }
        }
    }

    confirm?.let { a ->
        ConfirmSystemDialog(a, onConfirm = { vm.freeze(a.pkg); confirm = null }, onDismiss = { confirm = null })
    }
}

@Composable
private fun AppRow(app: AppItem, wl: Boolean, vm: MainVM, modifier: Modifier, askSystem: () -> Unit) {
    Row(
        modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Color(0x1ABFE6FF))
            .border(1.dp, Color(0x33BFE6FF), RoundedCornerShape(22.dp)).padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIcon(app.pkg)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(app.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(app.pkg, color = Accent.copy(alpha = .8f), fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (app.isUser) Tag("PENGGUNA", TagUser, Ice) else Tag("SISTEM", TagSys, Color(0xFFFCD34D))
                if (app.frozen) Tag("BEKU", TagFrz, Color(0xFFFDA4AF)) else Tag("AKTIF", TagAct, Color(0xFF6EE7B7))
                if (wl) Tag("WHITELIST", TagWht, Color(0xFFFDE047))
            }
        }
        Icon(
            Icons.Filled.Star, null, tint = if (wl) Color(0xFFFACC15) else Ice.copy(alpha = .35f),
            modifier = Modifier.size(34.dp).clip(CircleShape).clickable { vm.toggleWhitelist(app.pkg) }.padding(6.dp)
        )
        Spacer(Modifier.width(4.dp))
        if (app.frozen) SmallBtn("Cairkan", BtnKind.Success) { vm.unfreeze(app.pkg) }
        else SmallBtn("Bekukan", BtnKind.Danger) { if (!app.isUser) askSystem() else vm.freeze(app.pkg) }
    }
}
