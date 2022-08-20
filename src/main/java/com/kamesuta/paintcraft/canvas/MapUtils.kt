package com.kamesuta.paintcraft.canvas

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

fun MapCanvas.saveToMapView() {
    val src = MapReflection.getCanvasBuffer(this)
    val dst = MapReflection.getMapBuffer(mapView)
    if (src != null && dst != null) {
        System.arraycopy(src, 0, dst, 0, dst.size)
    }
}

fun MapCanvas.loadFromMapView() {
    val src = MapReflection.getMapBuffer(mapView)
    val dst = MapReflection.getCanvasBuffer(this)
    if (src != null && dst != null) {
        System.arraycopy(src, 0, dst, 0, dst.size)
    }
}

fun createMapBuffer(): ByteArray {
    return ByteArray(mapSize * mapSize)
}

fun MapBuffer.setMapPixel(x: Int, y: Int, color: MapDye) {
    if (x < 0 || x >= mapSize || y < 0 || y >= mapSize) {
        return
    }

    val index = y * mapSize + x
    if (index < 0 || index >= size) {
        return
    }

    this[index] = color
}

fun MapBuffer.getMapPixel(x: Int, y: Int): MapDye {
    if (x < 0 || x >= mapSize || y < 0 || y >= mapSize) {
        return 0
    }

    val index = y * mapSize + x
    if (index < 0 || index >= size) {
        return 0
    }

    return this[index]
}
