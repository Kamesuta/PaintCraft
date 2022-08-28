package com.kamesuta.paintcraft.map.draw

import com.kamesuta.paintcraft.PaintCraft
import com.kamesuta.paintcraft.map.DrawableMapBuffer
import com.kamesuta.paintcraft.map.DrawableMapBuffer.Companion.mapSize
import org.bukkit.Bukkit
import org.bukkit.map.MapCanvas

class DrawFill(
    private val x: Int,
    private val y: Int,
    private val srcColor: Byte,
    private val newColor: Byte,
) : Draw {
    override fun draw(canvas: MapCanvas) {
        Bukkit.getScheduler().runTaskAsynchronously(PaintCraft.instance) { ->
            val colored = DrawableMapBuffer()
            fillBucket(canvas, colored, x, y, srcColor, newColor)
        }
    }

    private fun fillBucket(
        canvas: MapCanvas,
        colored: DrawableMapBuffer,
        x: Int,
        y: Int,
        sourceColor: Byte,
        newColor: Byte
    ) {
        if (x < 0 || y < 0) {
            return
        }
        if (x >= mapSize || y >= mapSize) {
            return
        }
        if (colored[x, y] != 0.toByte()) {
            return
        }
        if (canvas.getPixel(x, y) != sourceColor) {
            return
        }
        canvas.setPixel(x, y, newColor)
        colored[x, y] = 1.toByte()

        fillBucket(canvas, colored, x - 1, y, sourceColor, newColor)
        fillBucket(canvas, colored, x + 1, y, sourceColor, newColor)
        fillBucket(canvas, colored, x, y - 1, sourceColor, newColor)
        fillBucket(canvas, colored, x, y + 1, sourceColor, newColor)
    }
}