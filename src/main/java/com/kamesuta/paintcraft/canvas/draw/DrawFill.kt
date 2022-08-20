package com.kamesuta.paintcraft.canvas.draw

import com.kamesuta.paintcraft.PaintCraft
import com.kamesuta.paintcraft.canvas.*
import org.bukkit.Bukkit
import org.bukkit.map.MapCanvas

class DrawFill(
    private val x: Int,
    private val y: Int,
    private val srcColor: MapDye,
    private val newColor: MapDye,
) : Draw {
    override fun draw(canvas: MapCanvas) {
        val colored = createMapBuffer()
        Bukkit.getScheduler().runTaskAsynchronously(PaintCraft.instance) { ->
            fillBucket(canvas, colored, x, y, srcColor, newColor)
        }
    }

    private fun fillBucket(canvas: MapCanvas, colored: MapBuffer, x: Int, y: Int, sourceColour: Byte, newColour: Byte) {
        if (x < 0 || y < 0) {
            return
        }
        if (x >= mapSize || y >= mapSize) {
            return
        }
        if (colored.getMapPixel(x, y) != 0.toByte()) {
            return
        }
        if (canvas.getPixel(x, y) != sourceColour) {
            return
        }
        canvas.setPixel(x, y, newColour)
        colored.setMapPixel(x, y, 1.toByte())

        fillBucket(canvas, colored, x - 1, y, sourceColour, newColour)
        fillBucket(canvas, colored, x + 1, y, sourceColour, newColour)
        fillBucket(canvas, colored, x, y - 1, sourceColour, newColour)
        fillBucket(canvas, colored, x, y + 1, sourceColour, newColour)
    }
}