package com.kamesuta.paintcraft.canvas

import org.bukkit.entity.Player
import org.bukkit.map.MapCanvas
import org.bukkit.map.MapRenderer
import org.bukkit.map.MapView
import java.awt.Graphics2D
import java.awt.image.BufferedImage

class MapRenderer : MapRenderer() {
    private val img: BufferedImage = BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB)
    var markDirty: Boolean = false

    override fun render(map: MapView, canvas: MapCanvas, player: Player) {
        if (markDirty) {
            repeat(canvas.cursors.size()) {
                canvas.cursors.removeCursor(canvas.cursors.getCursor(0))
            }

            canvas.drawImage(0, 0, img)

            markDirty = false
        }
    }

    fun flush(f: (Graphics2D) -> Unit) {
        f(img.createGraphics())
        markDirty = true
    }
}