package com.kamesuta.paintcraft.canvas

import com.kamesuta.paintcraft.canvas.draw.Draw
import org.bukkit.entity.Player
import org.bukkit.map.MapCanvas
import org.bukkit.map.MapRenderer
import org.bukkit.map.MapView
import java.util.concurrent.ConcurrentLinkedQueue

class MapRenderer : MapRenderer() {
    private var loaded = false
    private val draws = ConcurrentLinkedQueue<Draw>()

    override fun render(map: MapView, canvas: MapCanvas, player: Player) {
        if (!loaded) {
            canvas.loadFromMapView()
            loaded = true
        }

        if (!draws.isEmpty()) {
            repeat(canvas.cursors.size()) {
                canvas.cursors.removeCursor(canvas.cursors.getCursor(0))
            }

            while (!draws.isEmpty()) {
                draws.poll().draw(canvas)
            }

            canvas.saveToMapView()
        }
    }

    fun draw(draw: Draw) {
        draws.add(draw)
    }
}