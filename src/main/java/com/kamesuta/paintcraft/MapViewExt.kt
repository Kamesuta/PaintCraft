package com.kamesuta.paintcraft

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.map.MapRenderer
import org.bukkit.map.MapView
import java.util.function.Consumer

fun MapView.readData(): ByteArray {
    return Reflection.getMapCache(this)
}

fun MapView.setRenderer(renderer: MapRenderer?) {
    renderers.forEach(Consumer { removeRenderer(it) })
    if (renderer != null) {
        addRenderer(renderer)
    }
}

inline fun <reified Renderer: MapRenderer> MapView.getRenderer(): Renderer? {
    return renderers.filterIsInstance<Renderer>().firstOrNull()
}

fun MapView.update(player: Player) {
    Bukkit.getScheduler().runTask(PaintCraft.instance, Runnable { player.sendMap(this) })
}
