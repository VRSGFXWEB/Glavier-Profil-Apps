package com.glacier.profilapps

import android.content.Context
import kotlin.system.exitProcess

/** Dijalankan oleh Shizuku sebagai user shell (ADB) atau root. */
class UserService() : IUserService.Stub() {
    @Suppress("UNUSED_PARAMETER")
    constructor(context: Context) : this()

    override fun destroy() { exitProcess(0) }

    override fun exec(cmd: String?): String = try {
        val p = ProcessBuilder("sh", "-c", cmd ?: "").redirectErrorStream(true).start()
        val out = p.inputStream.bufferedReader().use { it.readText() }
        p.waitFor()
        out
    } catch (e: Exception) { "ERR: ${e.message}" }
}
