package com.kamesuta.paintcraft.map

import com.kamesuta.paintcraft.PaintCraft
import com.kamesuta.paintcraft.map.draw.Draw
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.map.MapView
import org.bukkit.persistence.PersistentDataType

class MapItem(val itemStack: ItemStack, val mapView: MapView, val renderer: MapRenderer) {
    fun draw(f: (g: (draw: Draw) -> Unit) -> Unit) {
        f(renderer::draw)
    }

    companion object {
        private val PAINT_GROUP_ID = NamespacedKey(PaintCraft.instance, "paint_group_id")
        private var ItemMeta.paintGroupId: Int?
            get() {
                return persistentDataContainer.get(PAINT_GROUP_ID, PersistentDataType.INTEGER)
            }
            set(value) {
                if (value != null) {
                    persistentDataContainer.set(PAINT_GROUP_ID, PersistentDataType.INTEGER, value)
                } else {
                    persistentDataContainer.remove(PAINT_GROUP_ID)
                }
            }

        fun create(world: World): MapItem {
            val mapView = Bukkit.getServer().createMap(world)
            val renderer = MapRenderer()
            mapView.setRenderer(renderer)
            mapView.centerX = -999999
            mapView.centerZ = -999999
            mapView.scale = MapView.Scale.FARTHEST
            val item = ItemStack(Material.FILLED_MAP)
            item.editMeta {
                it as MapMeta
                it.mapView = mapView
                it.paintGroupId = 1
            }
            return MapItem(item, mapView, renderer)
        }

        fun get(item: ItemStack): MapItem? {
            if (item.type != Material.FILLED_MAP)
                return null
            val mapView = (item.itemMeta as? MapMeta)?.mapView
                ?: return null
            val mapRenderer = mapView.getRenderer<MapRenderer>()
            val renderer = if (mapRenderer != null) {
                mapRenderer
            } else {
                item.itemMeta.paintGroupId
                    ?: return null

                val renderer = MapRenderer()
                mapView.setRenderer(renderer)
                renderer
            }
            return MapItem(item, mapView, renderer)
        }
    }
}