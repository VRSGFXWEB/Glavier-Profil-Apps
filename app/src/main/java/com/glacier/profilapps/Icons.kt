package com.glacier.profilapps

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/** Icon stroke yang sama dengan SVG di versi HTML. */
object Ic {
    private fun b(name: String, vararg d: String): ImageVector {
        val bld = ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f)
        d.forEach { p ->
            try {
                bld.addPath(
                    PathParser().parsePathString(p).toNodes(),
                    stroke = SolidColor(Color.Black), strokeLineWidth = 2.2f,
                    strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round
                )
            } catch (e: Exception) { }
        }
        return bld.build()
    }

    private fun circle(cx: Float, cy: Float, r: Float) = "M${cx - r} ${cy}a$r $r 0 1 0 ${2 * r} 0a$r $r 0 1 0 ${-2 * r} 0"
    private fun line(x1: Float, y1: Float, x2: Float, y2: Float) = "M$x1 ${y1}L$x2 $y2"
    private fun poly(pts: String): String {
        val n = pts.trim().split(" ")
        val sb = StringBuilder()
        var i = 0
        while (i + 1 < n.size) { sb.append(if (i == 0) "M" else "L").append(n[i]).append(' ').append(n[i + 1]); i += 2 }
        return sb.toString()
    }
    private fun rect(x: Float, y: Float, w: Float, h: Float, r: Float) =
        "M${x + r} ${y}h${w - 2 * r}a$r $r 0 0 1 $r ${r}v${h - 2 * r}a$r $r 0 0 1 ${-r} ${r}h${-(w - 2 * r)}a$r $r 0 0 1 ${-r} ${-r}v${-(h - 2 * r)}a$r $r 0 0 1 $r ${-r}z"

    val Home by lazy { b("home", "M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z", poly("9 22 9 12 15 12 15 22")) }
    val Pkg by lazy { b("pkg", "M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z", "M3.27 6.96L12 12.01 20.73 6.96", "M12 22.08V12") }
    val Brush by lazy { b("brush", "m9.06 11.9 8.07-8.06a2.85 2.85 0 1 1 4.03 4.03l-8.06 8.08", "M7.07 14.94c-1.66 0-3 1.34-3 3 0 1.31-1.16 2-2.07 2 1.31.81 3.07 1 4.07 0 1.66-1.66 1-3.07 1-5z") }
    val Shield by lazy { b("shield", "M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z") }
    val Sync by lazy { b("sync", "M21.5 2v6h-6M2.5 22v-6h6M2 11.5a10 10 0 0 1 18.8-4.3M22 12.5a10 10 0 0 1-18.8 4.2") }
    val Book by lazy { b("book", "M4 19.5v-15A2.5 2.5 0 0 1 6.5 2H20v20H6.5a2.5 2.5 0 0 1-2.5-2.5Z", "M6 2v20", "M10 6h6M10 10h6") }
    val Star by lazy { b("star", "M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z") }
    val Check by lazy { b("check", poly("20 6 9 17 4 12")) }
    val Chevron by lazy { b("chev", poly("9 18 15 12 9 6")) }
    val Search by lazy { b("search", circle(11f, 11f, 8f), "M21 21l-4.35-4.35") }
    val Close by lazy { b("close", line(18f, 6f, 6f, 18f), line(6f, 6f, 18f, 18f)) }
    val User by lazy { b("user", "M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2", circle(12f, 7f, 4f)) }
    val Cpu by lazy { b("cpu", rect(4f, 4f, 16f, 16f, 2f), rect(9f, 9f, 6f, 6f, 1f), "M9 1v3M15 1v3M9 20v3M15 20v3M20 9h3M20 14h3M1 9h3M1 14h3") }
    val Device by lazy { b("device", rect(5f, 2f, 14f, 20f, 2f), line(12f, 18f, 12.01f, 18f)) }
    val Clock by lazy { b("clock", circle(12f, 12f, 10f), poly("12 6 12 12 16 14")) }
    val Gamepad by lazy { b("game", rect(2f, 6f, 20f, 12f, 2f), "M6 12h4M8 10v4M15 13h.01M18 11h.01") }
    val Reset by lazy { b("reset", "M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8", "M3 3v5h5") }
    val Save by lazy { b("save", "M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z", poly("17 21 17 13 7 13 7 21"), poly("7 3 7 8 15 8")) }
    val Upload by lazy { b("upload", "M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4", poly("17 8 12 3 7 8"), line(12f, 3f, 12f, 15f)) }
    val Play by lazy { b("play", poly("5 3 19 12 5 21 5 3")) }
    val Zap by lazy { b("zap", poly("13 2 3 14 12 14 11 22 21 10 12 10 13 2")) }
    val Layers by lazy { b("layers", poly("12 2 2 7 12 12 22 7 12 2"), poly("2 17 12 22 22 17"), poly("2 12 12 17 22 12")) }
    val Freeze by lazy { b("freeze", "M12 2v20M17 5l-5 5-5-5M17 19l-5-5-5 5M2 12h20M5 7l5 5-5 5M19 7l-5 5 5 5") }
    val Ram by lazy { b("ram", rect(2f, 2f, 20f, 8f, 2f), rect(2f, 14f, 20f, 8f, 2f), line(6f, 6f, 6.01f, 6f), line(6f, 18f, 6.01f, 18f)) }
    val Alert by lazy { b("alert", "M21.73 18l-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z", line(12f, 9f, 12f, 13f), line(12f, 17f, 12.01f, 17f)) }
    val Bell by lazy { b("bell", "M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9", "M13.73 21a2 2 0 0 1-3.46 0") }
    val Heart by lazy { b("heart", "M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z") }
    val Call by lazy { b("call", "M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z") }
}
