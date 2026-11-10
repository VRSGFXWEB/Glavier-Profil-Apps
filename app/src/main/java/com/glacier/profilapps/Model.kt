package com.glacier.profilapps

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class AppItem(val pkg: String, val name: String, val isUser: Boolean, val frozen: Boolean)

data class GameCfg(
    val pkg: String,
    val renderer: String = "angle",
    val fps: String = "120",
    val speedCompile: Boolean = true,
    val touchSampling: Int = 300,
    val fastTouch: Int = 120,
) {
    fun toJson(): JSONObject = JSONObject().put("pkg", pkg).put("renderer", renderer).put("fps", fps)
        .put("speedCompile", speedCompile).put("touchSampling", touchSampling).put("fastTouch", fastTouch)

    companion object {
        fun from(o: JSONObject) = GameCfg(
            o.getString("pkg"), o.optString("renderer", "angle"), o.optString("fps", "120"),
            o.optBoolean("speedCompile", true), o.optInt("touchSampling", 300), o.optInt("fastTouch", 120)
        )
    }
}

data class Blocker(val pkg: String, val desc: String)

data class RamInfo(val totalKb: Long = 0, val usedKb: Long = 0, val availKb: Long = 0) {
    val pct: Int get() = if (totalKb > 0) (usedKb * 100 / totalKb).toInt() else 0
}

val PKG_REGEX = Regex("^[a-zA-Z0-9._]+$")

fun formatMemory(kb: Long): String =
    if (kb >= 1048576) String.format("%.1f GB", kb / 1048576.0) else "${kb / 1024} MB"

class Store(ctx: Context) {
    private val sp = ctx.getSharedPreferences("glacier", Context.MODE_PRIVATE)

    var dailyMode: String
        get() = sp.getString("daily", "balance") ?: "balance"
        set(v) { sp.edit().putString("daily", v).apply() }

    var smooth: Boolean
        get() = sp.getBoolean("smooth", false)
        set(v) { sp.edit().putBoolean("smooth", v).apply() }

    var whitelist: Set<String>
        get() = (sp.getStringSet("wl", emptySet()) ?: emptySet()).toSet()
        set(v) { sp.edit().putStringSet("wl", v).apply() }

    var games: List<GameCfg>
        get() = try {
            val a = JSONArray(sp.getString("games", "[]"))
            List(a.length()) { GameCfg.from(a.getJSONObject(it)) }
        } catch (e: Exception) { emptyList() }
        set(v) {
            val a = JSONArray(); v.forEach { a.put(it.toJson()) }
            sp.edit().putString("games", a.toString()).apply()
        }
}

val Throttlers = listOf(
    "com.miui.joyose" to "Xiaomi FPS Capper & Throttler",
    "com.miui.powerkeeper" to "MIUI Performance Limiter",
    "com.miui.analytics" to "MIUI Telemetry Tracker",
    "com.miui.msa.global" to "System Ad Services",
    "com.miui.cleanmaster" to "CleanMaster Background Daemon",
    "com.miui.securityadd" to "Security Center Subservice",
    "com.xiaomi.glgm" to "Xiaomi Games Daemon",
    "com.samsung.android.game.gos" to "Samsung Game Optimizing Service",
    "com.samsung.android.game.gametools" to "Game Tools Background",
    "com.samsung.android.game.gamehome" to "Game Launcher Analytics",
    "com.samsung.android.bixby.agent" to "Bixby Background Voice",
    "com.transsion.phonemaster" to "XOS/HiOS PhoneMaster Daemon",
    "com.transsion.hilauncher" to "Transsion Secondary Launcher",
    "com.transsion.magicshow" to "XTheme Advertising Daemon",
    "com.transsion.palmsore" to "PalmStore Auto Updater",
    "net.transsion.newsresourcemanager" to "News Resource Sync",
    "com.talpa.hibrowser" to "HiBrowser Background Tracker",
    "com.coloros.gamespace" to "ColorOS Game Space Analytics",
    "com.coloros.phonemanager" to "ColorOS Guard Daemon",
    "com.coloros.oppoguardelf" to "Oppo Guard Elf Throttler",
    "com.heytap.mcs" to "HeyTap Cloud Messaging",
    "com.heytap.market" to "HeyTap App Market Sync",
    "com.oplus.cosa" to "OPlus Background Agent",
    "com.vivo.gamewatch" to "Vivo GameWatch Throttler",
    "com.vivo.gamecube" to "Vivo GameCube Daemon",
    "com.vivo.easyshare" to "EasyShare Cloud Sync",
    "com.vivo.assistant" to "Jovi Assistant Daemon",
    "com.bbk.appstore" to "V-Appstore Analytics",
    "com.android.thermal" to "Thermal Engine Throttler",
    "com.qualcomm.qti.qdcm" to "Display Color Calibrator",
    "com.google.android.gms.policy_sidecar_oem_30_playstore" to "GMS Telemetry Tracker",
    "com.android.traceur" to "System Tracing Service",
    "com.google.android.feedback" to "Feedback Bug Reporter",
    "com.google.android.apps.wellbeing" to "Digital Wellbeing Daemon",
    "com.google.android.marvin.talkback" to "Accessibility Daemon",
    "com.facebook.katana" to "Facebook Analytics Service",
    "com.facebook.services" to "FB Background Daemon",
    "com.facebook.system" to "FB System Limiter",
    "com.facebook.appmanager" to "FB App Manager Tracker",
)
