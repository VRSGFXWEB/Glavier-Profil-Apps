package com.glacier.profilapps

import android.content.ComponentName
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import rikka.shizuku.Shizuku

/** Eksekusi command lewat root (su) atau Shizuku. */
object Sh {
    enum class Mode { NONE, ROOT, SHIZUKU }

    @Volatile var mode: Mode = Mode.NONE
    @Volatile var pkg: String = "com.glacier.profilapps"

    private var svc: IUserService? = null
    private var pending: CompletableDeferred<IUserService>? = null
    private val lock = Mutex()

    private val conn = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            if (binder != null && binder.pingBinder()) {
                val s = IUserService.Stub.asInterface(binder)
                svc = s
                pending?.complete(s)
            }
        }
        override fun onServiceDisconnected(name: ComponentName?) { svc = null }
    }

    fun shizukuRunning(): Boolean = try { Shizuku.pingBinder() } catch (e: Throwable) { false }

    fun shizukuGranted(): Boolean = try {
        Shizuku.pingBinder() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    } catch (e: Throwable) { false }

    fun shizukuUid(): Int = try { Shizuku.getUid() } catch (e: Throwable) { -1 }

    fun requestShizuku() { try { Shizuku.requestPermission(1001) } catch (e: Throwable) { } }

    suspend fun rootAvailable(): Boolean = try { runRoot("id").contains("uid=0") } catch (e: Exception) { false }

    /** pref: auto / root / shizuku */
    suspend fun detect(pref: String): Mode {
        val m = when (pref) {
            "root" -> if (rootAvailable()) Mode.ROOT else Mode.NONE
            "shizuku" -> if (shizukuGranted()) Mode.SHIZUKU else Mode.NONE
            else -> if (rootAvailable()) Mode.ROOT else if (shizukuGranted()) Mode.SHIZUKU else Mode.NONE
        }
        mode = m
        return m
    }

    suspend fun run(cmd: String): String = when (mode) {
        Mode.ROOT -> runRoot(cmd)
        Mode.SHIZUKU -> runShizuku(cmd)
        Mode.NONE -> throw IllegalStateException("Tidak ada akses")
    }

    private suspend fun runRoot(cmd: String): String = withContext(Dispatchers.IO) {
        val p = ProcessBuilder("su", "-c", cmd).redirectErrorStream(true).start()
        val out = p.inputStream.bufferedReader().use { it.readText() }
        p.waitFor()
        out
    }

    private suspend fun runShizuku(cmd: String): String {
        val s = service()
        return withContext(Dispatchers.IO) { s.exec(cmd) }
    }

    private suspend fun service(): IUserService = lock.withLock {
        svc?.let { if (it.asBinder().pingBinder()) return@withLock it }
        val d = CompletableDeferred<IUserService>()
        pending = d
        withContext(Dispatchers.Main) {
            val args = Shizuku.UserServiceArgs(ComponentName(pkg, UserService::class.java.name))
                .processNameSuffix("glacier").daemon(false).version(1)
            Shizuku.bindUserService(args, conn)
        }
        withTimeout(8000) { d.await() }
    }
}
