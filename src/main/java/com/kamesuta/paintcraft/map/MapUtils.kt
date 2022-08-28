package com.kamesuta.paintcraft.map

import com.kamesuta.paintcraft.canvas.CanvasUpdater
import org.bukkit.entity.Player
import org.bukkit.map.MapCanvas
import org.bukkit.map.MapRenderer
import org.bukkit.map.MapView
import java.util.function.Consumer

fun MapView.setRenderer(renderer: MapRenderer?) {
    renderers.forEach(Consumer { removeRenderer(it) })
    if (renderer != null) {
        addRenderer(renderer)
    }
}

inline fun <reified Renderer : MapRenderer> MapView.getRenderer(): Renderer? {
    return renderers.filterIsInstance<Renderer>().firstOrNull()
}

fun MapCanvas.updatePlayer(player: Player) {
    val dirty = MapReflection.getMapDirtyArea(player, mapView)
        ?: return
    val buffer = MapReflection.getCanvasBuffer(this)
        ?: return

    // プレイヤーに地図を送信する
    CanvasUpdater.sendMap(player, mapView, buffer, dirty)
}

fun MapCanvas.saveToMapView() {
    val src = MapReflection.getCanvasBuffer(this)
    val dst = MapReflection.getMapBuffer(mapView)
    if (src != null && dst != null) {
        src.copyTo(dst)
    }
}

fun MapCanvas.loadFromMapView() {
    val src = MapReflection.getMapBuffer(mapView)
    val dst = MapReflection.getCanvasBuffer(this)
    if (src != null && dst != null) {
        src.copyTo(dst)
    }
}
