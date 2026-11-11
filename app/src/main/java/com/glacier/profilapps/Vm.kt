package com.glacier.profilapps

import android.app.Application
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import rikka.shizuku.Shizuku
import java.io.File

class MainVM(app: Application) : AndroidViewModel(app) {
    private val ctx = app.applicationContext
    private val pm = ctx.packageManager
    private val store = Store(ctx)

    val protectedPkgs = setOf(
        "frb.axeron.manager", "com.android.systemui", "me.weishu.kernelsu",
        "com.topjohnwu.magisk", "io.github.a13e300.ksu", ctx.packageName
    )

    var apps by mutableStateOf<List<AppItem>>(emptyList()); private set
    var loading by mutableStateOf(false); private set
    var mode by mutableStateOf(Sh.Mode.NONE); private set
    var shizukuRunning by mutableStateOf(false); private set
    var shizukuGranted by mutableStateOf(false); private set
    var accessPref by mutableStateOf(store.accessPref); private set
    var dailyMode by mutableStateOf(store.dailyMode); private set
    var smooth by mutableStateOf(store.smooth); private set
    var whitelist by mutableStateOf(store.whitelist); private set
    var games by mutableStateOf(store.games); private set
    var ram by mutableStateOf(RamInfo()); private set
    var blockers by mutableStateOf<List<Blocker>>(emptyList()); private set
    var toastMsg by mutableStateOf(""); private set
    var toastTick by mutableIntStateOf(0); private set

    private val permL = Shizuku.OnRequestPermissionResultListener { _, _ -> viewModelScope.launch { refreshAccessNow() } }
    private val binderL = Shizuku.OnBinderReceivedListener { viewModelScope.launch { refreshAccessNow() } }
    private val deadL = Shizuku.OnBinderDeadListener { viewModelScope.launch { refreshAccessNow() } }

    init {
        try {
            Shizuku.addRequestPermissionResultListener(permL)
            Shizuku.addBinderReceivedListenerSticky(binderL)
            Shizuku.addBinderDeadListener(deadL)
        } catch (e: Throwable) { }
    }

    override fun onCleared() {
        try {
            Shizuku.removeRequestPermissionResultListener(permL)
            Shizuku.removeBinderReceivedListener(binderL)
            Shizuku.removeBinderDeadListener(deadL)
        } catch (e: Throwable) { }
        super.onCleared()
    }

    init {
        viewModelScope.launch {
            Sh.pkg = ctx.packageName
            refreshAccessNow()
            smooth = try {
                Settings.Global.getFloat(ctx.contentResolver, Settings.Global.WINDOW_ANIMATION_SCALE, 1f) in 0.4f..0.7f
            } catch (e: Exception) { store.smooth }
            loadAppsNow()
            refreshRam()
        }
    }

    fun toast(m: String) { toastMsg = m; toastTick++ }
    fun name(pkg: String) = apps.firstOrNull { it.pkg == pkg }?.name ?: pkg
    fun isFrozen(pkg: String) = apps.firstOrNull { it.pkg == pkg }?.frozen == true

    private suspend fun sh(cmd: String): String? {
        if (Sh.mode == Sh.Mode.NONE) refreshAccessNow()
        return try { Sh.run(cmd) } catch (e: Exception) { toast("Gagal: butuh akses root atau Shizuku"); null }
    }

    suspend fun refreshAccessNow() {
        shizukuRunning = Sh.shizukuRunning()
        shizukuGranted = Sh.shizukuGranted()
        mode = Sh.detect(accessPref)
    }

    fun recheckAccess() = viewModelScope.launch {
        refreshAccessNow()
        toast(when (mode) {
            Sh.Mode.ROOT -> "Akses root aktif"
            Sh.Mode.SHIZUKU -> "Akses Shizuku aktif"
            Sh.Mode.NONE -> "Belum ada akses root/Shizuku"
        })
    }

    fun setAccessPref(p: String) = viewModelScope.launch {
        accessPref = p; store.accessPref = p
        refreshAccessNow()
    }

    fun requestShizuku() = Sh.requestShizuku()

    fun refreshAll() = viewModelScope.launch {
        refreshAccessNow(); loadAppsNow(); refreshRam(); toast("Data diperbarui")
    }

    // ---------- Packages ----------
    fun loadApps() = viewModelScope.launch { loadAppsNow() }

    private suspend fun loadAppsNow() {
        loading = true
        apps = withContext(Dispatchers.IO) {
            pm.getInstalledApplications(0).map { ai ->
                AppItem(
                    ai.packageName, pm.getApplicationLabel(ai).toString(),
                    (ai.flags and ApplicationInfo.FLAG_SYSTEM) == 0, !ai.enabled
                )
            }.sortedBy { it.name.lowercase() }
        }
        loading = false
    }

    private fun setFrozen(pkgs: Set<String>, v: Boolean) {
        apps = apps.map { if (it.pkg in pkgs) it.copy(frozen = v) else it }
    }

    fun freeze(pkg: String) = viewModelScope.launch {
        if (pkg in protectedPkgs) { toast("${name(pkg)} dilindungi dan tidak boleh dibekukan!"); return@launch }
        toast("Membekukan ${name(pkg)}...")
        val out = sh("pm disable-user --user 0 $pkg") ?: return@launch
        if (out.contains("disabled")) { setFrozen(setOf(pkg), true); toast("${name(pkg)} berhasil dibekukan!") }
        else toast("Gagal membekukan ${name(pkg)}")
    }

    fun unfreeze(pkg: String) = viewModelScope.launch {
        toast("Mencairkan ${name(pkg)}...")
        val out = sh("pm enable $pkg") ?: return@launch
        if (out.contains("enabled")) { setFrozen(setOf(pkg), false); toast("${name(pkg)} berhasil dicairkan!") }
        else toast("Gagal mencairkan ${name(pkg)}")
    }

    fun unfreezeAll() = viewModelScope.launch {
        val l = apps.filter { it.frozen }.map { it.pkg }
        if (l.isEmpty()) return@launch
        toast("Mencairkan ${l.size} aplikasi...")
        sh(l.joinToString("; ") { "pm enable $it" }) ?: return@launch
        setFrozen(l.toSet(), false)
        toast("Seluruh aplikasi berhasil dicairkan!")
    }

    fun toggleWhitelist(pkg: String) {
        whitelist = if (pkg in whitelist) { toast("${name(pkg)} dihapus dari Whitelist"); whitelist - pkg }
        else { toast("${name(pkg)} ditambahkan ke Whitelist"); whitelist + pkg }
        store.whitelist = whitelist
    }

    // ---------- Home ----------
    fun applyDaily(mode: String) = viewModelScope.launch {
        dailyMode = mode; store.dailyMode = mode
        val cmd = when (mode) {
            "powersaver" -> "settings put global low_power 1; setprop debug.sf.hw 0 2>/dev/null"
            "extreme" -> "settings put global low_power 0; setprop debug.sf.hw 1; setprop debug.composition.type gpu; setprop debug.egl.hw 1"
            else -> "settings put global low_power 0; setprop debug.sf.hw 1"
        }
        toast("Mode Daily: ${mode.uppercase()} diterapkan")
        sh(cmd)
    }

    fun resetAll() = viewModelScope.launch {
        toast("Mereset seluruh setelan ke default...")
        val cmd = listOf(
            "settings put global window_animation_scale 1.0",
            "settings put global transition_animation_scale 1.0",
            "settings put global animator_duration_scale 1.0",
            "settings put global low_power 0",
            "settings delete global angle_enabled_pkgs 2>/dev/null",
            "settings delete global game_driver_opt_in 2>/dev/null",
            "settings delete global updatable_driver_production_opt_in 2>/dev/null",
            "settings put system user_refresh_rate 60",
            "settings put system peak_refresh_rate 60",
            "setprop debug.sf.hw 1",
            "setprop persist.vendor.touch.sampling 120 2>/dev/null",
            "setprop persist.sys.touch.rate 60 2>/dev/null"
        ).joinToString("; ")
        sh(cmd) ?: return@launch
        dailyMode = "balance"; store.dailyMode = "balance"
        smooth = false; store.smooth = false
        toast("Seluruh pengaturan berhasil di-reset!")
    }

    fun exportJson(): String = JSONObject().apply {
        put("version", "1.0"); put("daily_mode", dailyMode); put("smooth_ui", smooth)
        put("whitelist", JSONArray(whitelist.toList()))
        put("frozen_apps", JSONArray(apps.filter { it.frozen }.map { it.pkg }))
        put("gamespace", JSONArray().also { a -> games.forEach { a.put(it.toJson()) } })
    }.toString(2)

    fun exportTo(uri: Uri) = viewModelScope.launch {
        try {
            val json = exportJson()
            withContext(Dispatchers.IO) { ctx.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) } }
            toast("Pengaturan tersimpan ke glacier.json!")
        } catch (e: Exception) { toast("Gagal menyimpan file JSON.") }
    }

    fun importFrom(uri: Uri) = viewModelScope.launch {
        try {
            val text = withContext(Dispatchers.IO) {
                ctx.contentResolver.openInputStream(uri)?.use { String(it.readBytes()) } ?: ""
            }
            val o = JSONObject(text)
            if (o.has("daily_mode")) { dailyMode = o.getString("daily_mode"); store.dailyMode = dailyMode }
            if (o.has("smooth_ui")) { smooth = o.getBoolean("smooth_ui"); store.smooth = smooth }
            o.optJSONArray("whitelist")?.let { a ->
                whitelist = List(a.length()) { a.getString(it) }.filter { PKG_REGEX.matches(it) }.toSet(); store.whitelist = whitelist
            }
            o.optJSONArray("gamespace")?.let { a ->
                games = List(a.length()) { GameCfg.from(a.getJSONObject(it)) }.filter { PKG_REGEX.matches(it.pkg) }; store.games = games
            }
            o.optJSONArray("frozen_apps")?.let { a ->
                val l = List(a.length()) { a.getString(it) }.filter { PKG_REGEX.matches(it) && it !in protectedPkgs }
                if (l.isNotEmpty()) sh(l.joinToString("; ") { "pm disable-user --user 0 $it" })
            }
            loadAppsNow()
            toast("glacier.json berhasil diterapkan!")
        } catch (e: Exception) { toast("Gagal membaca file JSON.") }
    }

    // ---------- Game Space ----------
    fun addGame(pkg: String) {
        if (games.any { it.pkg == pkg }) { toast("Game sudah ada di Game Space!"); return }
        games = games + GameCfg(pkg); store.games = games
        toast("${name(pkg)} ditambahkan ke Game Space")
    }

    fun removeGame(pkg: String) {
        games = games.filter { it.pkg != pkg }; store.games = games
        toast("${name(pkg)} dihapus dari Game Space")
    }

    fun updateGame(g: GameCfg) {
        games = games.map { if (it.pkg == g.pkg) g else it }; store.games = games
    }

    fun injectGame(g: GameCfg) = viewModelScope.launch {
        toast("Menerapkan profil ke ${name(g.pkg)}...")
        val c = mutableListOf<String>()
        if (g.renderer == "angle") {
            c += "settings put global angle_enabled_pkgs ${g.pkg}"; c += "settings put global game_driver_opt_in 1"
        } else if (g.renderer == "vulkan") {
            c += "settings put global updatable_driver_production_opt_in ${g.pkg}"
        }
        c += "settings put system user_refresh_rate ${g.fps}"
        c += "settings put system peak_refresh_rate ${g.fps}"
        c += "setprop persist.vendor.touch.sampling ${g.touchSampling}"
        c += "setprop persist.sys.touch.rate ${g.fastTouch}"
        if (g.speedCompile) c += "cmd package compile -m speed -f ${g.pkg} >/dev/null 2>&1 &"
        sh(c.joinToString("; ")) ?: return@launch
        toast("Profil ${name(g.pkg)} berhasil diterapkan!")
    }

    fun launchGame(pkg: String) {
        val i = pm.getLaunchIntentForPackage(pkg)
        if (i == null) { toast("Gagal meluncurkan ${name(pkg)}"); return }
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ctx.startActivity(i)
    }

    // ---------- Cleaner ----------
    suspend fun refreshRam() {
        ram = withContext(Dispatchers.IO) {
            var t = 0L; var a = 0L; var f = 0L; var b = 0L; var c = 0L
            try {
                File("/proc/meminfo").forEachLine { l ->
                    val v = l.filter { ch -> ch.isDigit() }.toLongOrNull() ?: 0L
                    when {
                        l.startsWith("MemTotal:") -> t = v
                        l.startsWith("MemAvailable:") -> a = v
                        l.startsWith("MemFree:") -> f = v
                        l.startsWith("Buffers:") -> b = v
                        l.startsWith("Cached:") -> c = v
                    }
                }
            } catch (e: Exception) { }
            if (a == 0L) a = f + b + c
            RamInfo(t, (t - a).coerceAtLeast(0), a)
        }
    }

    fun stopBackground() = viewModelScope.launch {
        toast("Menghentikan layanan latar belakang...")
        val targets = apps.filter { it.isUser && it.pkg !in protectedPkgs && it.pkg !in whitelist }
        val kill = targets.joinToString("") { "am force-stop ${it.pkg}; " }
        sh("${kill}am kill-all; cmd activity compact all 2>/dev/null; echo 3 > /proc/sys/vm/drop_caches 2>/dev/null; sync") ?: return@launch
        toast("Layanan latar belakang dihentikan! RAM lebih lega.")
        refreshRam()
    }

    fun wipeCache() = viewModelScope.launch {
        toast("Menjalankan Deep Wipe Cache...")
        val cmd = "pm trim-caches 999G; rm -rf /sdcard/Android/data/*/cache/* /sdcard/Android/data/*/code_cache/* " +
            "/sdcard/Android/media/*/cache/* /sdcard/DCIM/.thumbnails/* /sdcard/Pictures/.thumbnails/* " +
            "/sdcard/LOST.DIR/* /sdcard/Download/.cache/* /data/local/tmp/* /sdcard/.trash/* 2>/dev/null; " +
            "echo 3 > /proc/sys/vm/drop_caches 2>/dev/null; sync"
        sh(cmd) ?: return@launch
        toast("Deep Wipe Cache selesai!")
        refreshRam()
    }

    fun toggleSmooth() = viewModelScope.launch {
        smooth = !smooth; store.smooth = smooth
        val v = if (smooth) "0.5" else "1.0"
        toast(if (smooth) "Menyuntikkan tweak smooth launcher..." else "Mengembalikan animasi default...")
        var cmd = "settings put global window_animation_scale $v; settings put global transition_animation_scale $v; settings put global animator_duration_scale $v"
        if (smooth) cmd += "; setprop debug.sf.hw 1; setprop debug.egl.hw 1; setprop debug.composition.type gpu"
        sh(cmd) ?: return@launch
        toast(if (smooth) "Smooth Launcher Tweaks aktif!" else "Pengaturan dinonaktifkan.")
    }

    // ---------- Saran ----------
    fun scanBlockers() = viewModelScope.launch {
        val installed = apps.map { it.pkg }.toSet()
        val fps = Throttlers.filter { it.first in installed }.map { Blocker(it.first, it.second) }
        val running = try { Sh.run("ps -A -o NAME").lines().map { it.trim() }.toSet() } catch (e: Exception) { emptySet() }
        val bg = apps.filter { it.isUser && it.pkg in running && it.pkg !in protectedPkgs && it.pkg !in whitelist }
            .map { Blocker(it.pkg, "Aktif di Background (Beban RAM)") }
        blockers = (fps + bg).distinctBy { it.pkg }
    }

    fun freezeAllBlockers() = viewModelScope.launch {
        val t = blockers.map { it.pkg }.filter { it !in protectedPkgs && !isFrozen(it) }
        if (t.isEmpty()) { toast("Tidak ada yang perlu dibekukan"); return@launch }
        toast("Membekukan semua pembatas performa & background...")
        sh(t.joinToString("; ") { "pm disable-user --user 0 $it" }) ?: return@launch
        setFrozen(t.toSet(), true)
        toast("Seluruh pembatas berhasil dibekukan!")
    }
}
