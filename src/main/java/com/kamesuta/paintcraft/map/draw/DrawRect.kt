package com.kamesuta.paintcraft.map.draw

import org.bukkit.map.MapCanvas
import kotlin.math.max
import kotlin.math.min

class DrawRect(
    private val x1: Int,
    private val y1: Int,
    private val x2: Int,
    private val y2: Int,
    private val color: Byte,
    private val fill: Boolean,
) : Draw {
    override fun draw(canvas: MapCanvas) {
        val x1 = min(x1, x2)
        val y1 = min(y1, y2)
        val x2 = max(x1, x2)
        val y2 = max(y1, y2)

        if (fill) {
            for (x in x1..x2) {
                for (y in y1..y2) {
                    canvas.setPixel(x, y, color)
                }
            }
        } else {
            for (x in x1..x2) {
                canvas.setPixel(x, y1, color)
                canvas.setPixel(x, y2, color)
            }
            for (y in y1..y2) {
                canvas.setPixel(x1, y, color)
                canvas.setPixel(x2, y, color)
            }
        }
    }
}