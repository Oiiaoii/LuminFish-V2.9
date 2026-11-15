package com.solmacro

// v2.5 PIXEL RECOGNITION - the method macro scripts use (PixelSearch): look for a colour on the screen, then check the shape.
//   Fish button : blue-violet pixels forming a wide, flat block on a dark panel   -> tap its centre
//   Exit button : salmon-red pixels forming a wide, flat block on a dark panel    -> fishing is running
//   X button    : a small, lonely, white X (two diagonals, corners lit, edge middles empty) in the upper right of the box
// It needs no pictures and no OpenCV, works at any screen size (sizes are relative), and takes a few milliseconds.
// Scene.kt asks it first and falls back to the picture matching when it sees nothing.
object Pix {
    private class Comp(val x0: Int, val y0: Int, val x1: Int, val y1: Int, val n: Int)

    private fun isKind(kind: Int, p: Int): Boolean {
        val r = (p shr 16) and 0xFF
        val g = (p shr 8) and 0xFF
        val b = p and 0xFF
        return when (kind) {
            Scene.FISH -> b >= 95 && r >= 50 && b - r >= 28 && b - g >= 22 && r <= 150
            Scene.EXIT -> r >= 60 && r - g >= 12 && r >= b && g <= 190
            else -> {
                val mx = maxOf(r, maxOf(g, b))
                val mn = minOf(r, minOf(g, b))
                mn >= 215 && mx - mn <= 35
            }
        }
    }

    private fun maskOf(reg: Reg, kind: Int, rows: Int): ByteArray {
        val m = ByteArray(reg.w * rows)
        val px = reg.px
        for (i in 0 until reg.w * rows) if (isKind(kind, px[i])) m[i] = 1
        return m
    }

    // groups the matching pixels: cells of cell x cell pixels count as active with minAct matches, cells within rad cells join.
    private fun comps(m: ByteArray, w: Int, hh: Int, cell: Int, rad: Int, minAct: Int): List<Comp> {
        val ch = hh / cell
        val cw = w / cell
        if (ch < 1 || cw < 1) return emptyList()
        val cnt = IntArray(ch * cw)
        for (y in 0 until ch * cell) {
            val row = y * w
            val cy = (y / cell) * cw
            for (x in 0 until cw * cell) if (m[row + x].toInt() != 0) cnt[cy + x / cell]++
        }
        val seen = BooleanArray(ch * cw)
        val stack = IntArray(ch * cw)
        val out = ArrayList<Comp>()
        for (c0 in 0 until ch * cw) {
            if (cnt[c0] < minAct || seen[c0]) continue
            var sp = 0
            stack[sp++] = c0
            seen[c0] = true
            var minX = cw
            var maxX = -1
            var minY = ch
            var maxY = -1
            var n = 0
            while (sp > 0) {
                val c = stack[--sp]
                val cy = c / cw
                val cx = c % cw
                n += cnt[c]
                if (cx < minX) minX = cx
                if (cx > maxX) maxX = cx
                if (cy < minY) minY = cy
                if (cy > maxY) maxY = cy
                for (dy in -rad..rad) {
                    val yy = cy + dy
                    if (yy < 0 || yy >= ch) continue
                    for (dx in -rad..rad) {
                        val xx = cx + dx
                        if (xx < 0 || xx >= cw) continue
                        val k = yy * cw + xx
                        if (cnt[k] >= minAct && !seen[k]) {
                            seen[k] = true
                            stack[sp++] = k
                        }
                    }
                }
            }
            out.add(Comp(minX * cell, minY * cell, (maxX + 1) * cell, (maxY + 1) * cell, n))
        }
        return out
    }

    private fun sumRect(m: ByteArray, w: Int, x0: Int, y0: Int, x1: Int, y1: Int): Int {
        var s = 0
        for (y in y0 until y1) {
            val row = y * w
            for (x in x0 until x1) s += m[row + x].toInt()
        }
        return s
    }

    // the best block of the colour that has the shape of a button (wide and flat) on a dark panel: centre x, centre y, matching pixels
    private fun button(reg: Reg, kind: Int): IntArray? {
        val w = reg.w
        val hh = reg.h
        val m = maskOf(reg, kind, hh)
        val cell = maxOf(4, w / 160)
        var best: IntArray? = null
        for (c in comps(m, w, hh, cell, 2, 2)) {
            val bw = c.x1 - c.x0
            val bh = c.y1 - c.y0
            if (bw < 40 || bh < 8 || c.n < 60) continue
            val asp = bw.toDouble() / bh
            if (asp < 3.5 || asp > 9.0) continue
            var sum = 0L
            for (y in c.y0 until minOf(c.y1, hh)) {
                val row = y * w
                for (x in c.x0 until minOf(c.x1, w)) {
                    val p = reg.px[row + x]
                    sum += (((p shr 16) and 0xFF) * 3 + ((p shr 8) and 0xFF) * 6 + (p and 0xFF)) / 10
                }
            }
            val area = (minOf(c.y1, hh) - c.y0) * (minOf(c.x1, w) - c.x0)
            if (area <= 0 || sum.toDouble() / area > 85.0) continue
            val b = best
            if (b == null || c.n > b[2]) best = intArrayOf((c.x0 + c.x1) / 2, (c.y0 + c.y1) / 2, c.n)
        }
        return best
    }

    // the white X in the upper part of the screen: centre x, centre y
    private fun xButton(reg: Reg): IntArray? {
        val w = reg.w
        val lim = (reg.h * 0.6).toInt()
        if (lim < 20) return null
        val m = maskOf(reg, Scene.XBTN, lim)
        fun on(px: Int, py: Int): Boolean {
            for (dy in -1..1) for (dx in -1..1) {
                val yy = py + dy
                val xx = px + dx
                if (yy in 0 until lim && xx in 0 until w && m[yy * w + xx].toInt() != 0) return true
            }
            return false
        }
        var best: IntArray? = null
        var bestScore = -1.0e9
        for (c in comps(m, w, lim, 3, 1, 1)) {
            val bw = c.x1 - c.x0
            val bh = c.y1 - c.y0
            if (bw < 10 || bh < 10 || bw > 160 || bh > 160) continue
            val asp = bw.toDouble() / bh
            if (asp < 0.75 || asp > 1.33) continue
            if ((c.x0 + c.x1) / 2.0 < w * 0.45) continue
            val fill = c.n.toDouble() / (bw * bh)
            if (fill < 0.06 || fill > 0.5) continue
            // both diagonals must be lit
            var ok = 0
            var tot = 0
            for (i in 1..9) {
                val t = 0.15 + 0.7 * (i - 1) / 8.0
                val px = (c.x0 + t * (bw - 1)).toInt()
                val py = (c.y0 + t * (bh - 1)).toInt()
                val qx = (c.x1 - 1 - t * (bw - 1)).toInt()
                tot += 2
                if (on(px, py)) ok++
                if (on(qx, py)) ok++
            }
            if (ok < tot * 0.75) continue
            val cx = (c.x0 + c.x1) / 2
            val cy = (c.y0 + c.y1) / 2
            // the middles of the four edges must be empty (an X, not a square, a plus or a crosshair)
            var edges = 0
            if (on(cx, c.y0 + 1)) edges++
            if (on(cx, c.y1 - 2)) edges++
            if (on(c.x0 + 1, cy)) edges++
            if (on(c.x1 - 2, cy)) edges++
            if (edges > 1) continue
            // and the four corners must be lit
            val ci = maxOf(1, (0.12 * minOf(bw, bh)).toInt())
            if (!(on(c.x0 + ci, c.y0 + ci) && on(c.x1 - 1 - ci, c.y0 + ci) && on(c.x0 + ci, c.y1 - 1 - ci) && on(c.x1 - 1 - ci, c.y1 - 1 - ci))) continue
            // it stands alone: letters of a text have white neighbours close by, an X does not
            val mg = maxOf(3, (0.3 * maxOf(bw, bh)).toInt())
            val ya = maxOf(0, c.y0 - mg)
            val yb = minOf(lim, c.y1 + mg)
            val xa = maxOf(0, c.x0 - mg)
            val xb = minOf(w, c.x1 + mg)
            val ring = sumRect(m, w, xa, ya, xb, yb) - sumRect(m, w, c.x0, c.y0, minOf(c.x1, w), minOf(c.y1, lim))
            if (ring > 0.02 * ((yb - ya) * (xb - xa) - bw * bh)) continue
            val score = (c.x0 + c.x1) / 2.0 - (c.y0 + c.y1) / 4.0
            if (score > bestScore) {
                bestScore = score
                best = intArrayOf(cx, cy)
            }
        }
        return best
    }

    // want = Scene.FISH / Scene.EXIT / Scene.XBTN (can be combined). Centre in real screen pixels, like Scene.find.
    // A seen X wins (a result box covers the buttons), otherwise the bigger button block wins.
    fun find(reg: Reg, want: Int): Scene.Hit? {
        if (reg.w < 32 || reg.h < 32 || reg.px.size < reg.w * reg.h) return null
        if (want and Scene.XBTN != 0) {
            val x = xButton(reg)
            if (x != null) return Scene.Hit(Scene.XBTN, reg.x + x[0] * reg.step, reg.y + x[1] * reg.step, 0.95)
        }
        var out: Scene.Hit? = null
        var outN = 0
        if (want and Scene.FISH != 0) {
            val b = button(reg, Scene.FISH)
            if (b != null) { out = Scene.Hit(Scene.FISH, reg.x + b[0] * reg.step, reg.y + b[1] * reg.step, 0.80 + 0.19 * minOf(1.0, b[2] / 300.0)); outN = b[2] }
        }
        if (want and Scene.EXIT != 0) {
            val b = button(reg, Scene.EXIT)
            if (b != null && b[2] > outN) out = Scene.Hit(Scene.EXIT, reg.x + b[0] * reg.step, reg.y + b[1] * reg.step, 0.80 + 0.19 * minOf(1.0, b[2] / 300.0))
        }
        return out
    }
}
