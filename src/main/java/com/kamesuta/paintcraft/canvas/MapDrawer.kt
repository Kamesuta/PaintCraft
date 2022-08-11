package com.kamesuta.paintcraft.canvas

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.MapMeta
import java.awt.Graphics2D

class MapDrawer(private val stack: ItemStack) {
    fun draw(f: (Graphics2D) -> Unit) {
        val meta = stack.itemMeta as? MapMeta
        if (meta != null) {
            val view = meta.mapView
            if (view != null) {
                val renderer = view.renderers.filterIsInstance<MapRenderer>().firstOrNull()
                if (renderer != null) {
                    renderer.flush(f)
                } else {
                    val newRenderer = MapRenderer()
                    view.addRenderer(newRenderer)
                    newRenderer.flush(f)
                }
            }
        }
    }


    companion object {
        fun genNew(world: World): Pair<ItemStack, MapDrawer> {
            val mapView = Bukkit.getServer().createMap(world)
            // toListで一旦コピーしてから、リストの要素を削除すると、ConcurrentModificationExceptionが発生しない
            mapView.renderers.toList().forEach { mapView.removeRenderer(it) }
            val mapStack = ItemStack(Material.FILLED_MAP)
            mapStack.editMeta {
                it as MapMeta
                it.mapView = mapView
            }
            return mapStack to MapDrawer(mapStack)
        }
    }
}