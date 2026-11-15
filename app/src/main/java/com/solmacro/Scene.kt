package com.solmacro

import android.content.Context
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.core.Size
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.nio.ByteBuffer
import java.nio.ByteOrder

// v2.3 screen recognition of the fishing buttons, from the pictures in assets/scene:
//   fish.png   the "Fish" button  -> tap it
//   exit.png   the "Exit" button  -> fishing is running, do NOT tap there (it would leave the fishing)
//   x_icon.png the X in the top-right corner of the "Fishing Failed" / "Fish Caught!" box -> tap it
// The match is done at many sizes (phones differ), and the size that worked is remembered so the next look is fast.
object Scene {
    const val FISH = 1
    const val EXIT = 2
    const val XBTN = 4

    class Hit(val kind: Int, val x: Int, val y: Int, val score: Double)

    private var tFish: Mat? = null
    private var tExit: Mat? = null
    private var tX: Mat? = null
    private var loaded = false
    private val memo = DoubleArray(3)          // last good scale per template
    @Volatile var info = ""

    private fun load(ctx: Context, name: String): Mat? = try {
        val b = ctx.assets.open("scene/$name").readBytes()
        val m = Imgcodecs.imdecode(MatOfByte(*b), Imgcodecs.IMREAD_GRAYSCALE)
        if (m.empty()) null else m
    } catch (e: Throwable) {
        Dbg.add("Scene picture $name not loaded: " + e.message)
        null
    }

    @Synchronized
    private fun ensure(ctx: Context) {
        if (loaded) return
        tFish = load(ctx, "fish.png")
        tExit = load(ctx, "exit.png")
        tX = load(ctx, "x_icon.png")
        loaded = true
    }

    private class Best(val score: Double, val x: Int, val y: Int, val w: Int, val h: Int, val scale: Double)

    // best match of tpl in g (grayscale). Tries the remembered scale first, then sweeps 0.05 .. 1.2 of the template size.
    private fun best(g: Mat, tpl: Mat, slot: Int, accept: Double): Best? {
        fun at(s: Double): Best? {
            val tw = (tpl.cols() * s).toInt()
            val th = (tpl.rows() * s).toInt()
            if (th < 10 || tw < 10 || th >= g.rows() || tw >= g.cols()) return null
            val t = Mat()
            val r = Mat()
            try {
                Imgproc.resize(tpl, t, Size(tw.toDouble(), th.toDouble()), 0.0, 0.0, Imgproc.INTER_AREA)
                Imgproc.matchTemplate(g, t, r, Imgproc.TM_CCOEFF_NORMED)
                val mm = Core.minMaxLoc(r)
                return Best(mm.maxVal, mm.maxLoc.x.toInt(), mm.maxLoc.y.toInt(), tw, th, s)
            } finally {
                t.release()
                r.release()
            }
        }
        if (memo[slot] > 0.0) {
            val b = at(memo[slot])
            if (b != null && b.score >= accept) return b
        }
        var top: Best? = null
        var s = 0.05
        while (s <= 1.2) {
            val b = at(s)
            if (b != null && (top == null || b.score > top.score)) top = b
            s *= 1.14
        }
        if (top != null && top.score >= accept) memo[slot] = top.scale
        return top
    }

    // reg = the whole screen (any sampling step). want = FISH or EXIT or XBTN (can be combined).
    // Returns the best recognised thing with the centre in real screen pixels, or null.
    @Synchronized
    fun find(ctx: Context, reg: Reg, want: Int, pixelFirst: Boolean = true): Hit? {
        // v2.5: pixel recognition first (script-style colour search, few milliseconds); pictures only when it sees nothing
        if (pixelFirst && Cfg.get(ctx, "pixrec") == 1) {
            try {
                val p = Pix.find(reg, want)
                if (p != null) {
                    info = "Pixel " + (when (p.kind) { FISH -> "Fish"; EXIT -> "Exit"; else -> "X" }) + " " + (p.score * 100).toInt() + "%"
                    return p
                }
            } catch (e: Throwable) {
                Dbg.add("Pixel look failed: " + e.message)
            }
        }
        if (!Cv.ready) return null
        ensure(ctx)
        val n = reg.w * reg.h
        if (reg.px.size < n || reg.w < 32 || reg.h < 32) return null
        val bgra = Mat(reg.h, reg.w, CvType.CV_8UC4)
        val g = Mat()
        try {
            val arr = ByteArray(n * 4)
            ByteBuffer.wrap(arr).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().put(reg.px, 0, n)
            bgra.put(0, 0, arr)
            Imgproc.cvtColor(bgra, g, Imgproc.COLOR_BGRA2GRAY)
            var out: Hit? = null
            fun consider(kind: Int, b: Best?, ox: Int, oy: Int) {
                if (b == null) return
                val cx = reg.x + (ox + b.x + b.w / 2) * reg.step
                val cy = reg.y + (oy + b.y + b.h / 2) * reg.step
                if (out == null || b.score > out!!.score) out = Hit(kind, cx, cy, b.score)
            }
            if (want and XBTN != 0) {
                // the X sits in the upper part of the screen
                val ph = (g.rows() * 0.6).toInt()
                val sub = g.submat(0, ph, 0, g.cols())
                val b = tX?.let { best(sub, it, 2, 0.86) }
                if (b != null && b.score >= 0.86) consider(XBTN, b, 0, 0)
                sub.release()
            }
            if (want and FISH != 0) { val b = tFish?.let { best(g, it, 0, 0.82) }; if (b != null && b.score >= 0.82) consider(FISH, b, 0, 0) }
            if (want and EXIT != 0) { val b = tExit?.let { best(g, it, 1, 0.82) }; if (b != null && b.score >= 0.82) consider(EXIT, b, 0, 0) }
            info = if (out == null) "nothing" else (when (out!!.kind) { FISH -> "Fish"; EXIT -> "Exit"; else -> "X" }) + " " + (out!!.score * 100).toInt() + "%"
            return out
        } catch (e: Throwable) {
            Dbg.add("Scene error: " + e.message)
            return null
        } finally {
            bgra.release()
            g.release()
        }
    }
}
