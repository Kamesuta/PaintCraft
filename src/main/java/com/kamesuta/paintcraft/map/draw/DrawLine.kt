package com.kamesuta.paintcraft.map.draw

import org.bukkit.map.MapCanvas
import kotlin.math.abs

class DrawLine(
    private val x1: Int,
    private val y1: Int,
    private val x2: Int,
    private val y2: Int,
    private val color: Byte,
) : Draw {
    override fun draw(canvas: MapCanvas) {
        val w: Int = x2 - x1
        val h: Int = y2 - y1

        var dx1 = 0
        var dy1 = 0
        var dx2 = 0
        var dy2 = 0

        if (w != 0) {
            dx1 = if (w > 0) 1 else -1
            dx2 = if (w > 0) 1 else -1
        }

        if (h != 0) {
            dy1 = if (h > 0) 1 else -1
        }

        var longest = abs(w)
        var shortest = abs(h)

        if (longest <= shortest) {
            longest = abs(h)
            shortest = abs(w)
            if (h < 0) {
                dy2 = -1
            } else if (h > 0) {
                dy2 = 1
            }
            dx2 = 0
        }
        var numerator = longest shr 1

        var x = x1
        var y = y1
        for (i in 0..longest) {
            canvas.setPixel(x, y, color)
            numerator += shortest
            if (numerator >= longest) {
                numerator -= longest
                x += dx1
                y += dy1
            } else {
                x += dx2
                y += dy2
            }
        }
    }
}