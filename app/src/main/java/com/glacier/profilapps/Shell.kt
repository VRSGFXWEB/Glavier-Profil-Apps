package com.glacier.profilapps

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Eksekusi command lewat root (su). */
object Sh {
    suspend fun run(cmd: String): String = withContext(Dispatchers.IO) {
        val p = ProcessBuilder("su", "-c", cmd).redirectErrorStream(true).start()
        val out = p.inputStream.bufferedReader().use { it.readText() }
        p.waitFor()
        out
    }

    suspend fun hasRoot(): Boolean = try { run("id").contains("uid=0") } catch (e: Exception) { false }
}
