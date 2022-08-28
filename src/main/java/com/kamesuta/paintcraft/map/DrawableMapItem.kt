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

/**
 * 書き込み可能マップ
 * @param itemStack アイテム
 * @param mapView マップ
 * @param renderer レンダラー
 */
class DrawableMapItem(val itemStack: ItemStack, val mapView: MapView, val renderer: DrawableMapRenderer) {
    /**
     * マップに描画する
     * @param f 描画する関数
     */
    fun draw(f: (g: (draw: Draw) -> Unit) -> Unit) {
        f(renderer::draw)
    }

    companion object {
        /**
         * アイテムをマップとして認識するためのキー
         */
        private val PAINT_GROUP_ID = NamespacedKey(PaintCraft.instance, "paint_group_id")

        /**
         * 書き込み可能マップを作成する
         * @param world ワールド
         */
        fun create(world: World): DrawableMapItem {
            val mapView = Bukkit.getServer().createMap(world)
            val renderer = DrawableMapRenderer()
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
            return DrawableMapItem(item, mapView, renderer)
        }

        /**
         * 書き込み可能マップであれば取得する
         * @param item アイテム
         * @return 書き込み可能マップ
         */
        fun get(item: ItemStack): DrawableMapItem? {
            if (item.type != Material.FILLED_MAP)
                return null
            val mapView = (item.itemMeta as? MapMeta)?.mapView
                ?: return null
            val drawableMapRenderer = mapView.getRenderer()
            val renderer = if (drawableMapRenderer != null) {
                drawableMapRenderer
            } else {
                item.itemMeta.paintGroupId
                    ?: return null

                val renderer = DrawableMapRenderer()
                mapView.setRenderer(renderer)
                renderer
            }
            return DrawableMapItem(item, mapView, renderer)
        }

        /**
         * マップのグループID
         * TODO オーナーとか権限とかをグループIDで管理する
         */
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

        /**
         * マップのレンダラーを書き込み可能レンダラー置き換える
         * @param renderer レンダラー
         */
        private fun MapView.setRenderer(renderer: DrawableMapRenderer?) {
            // ConcurrentModificationExceptionが起きないように一度toListする
            renderers.toList().forEach { removeRenderer(it) }
            if (renderer != null) {
                addRenderer(renderer)
            }
        }

        /**
         * 書き込み可能レンダラーを取得する
         */
        private fun MapView.getRenderer(): DrawableMapRenderer? {
            return renderers.filterIsInstance<DrawableMapRenderer>().firstOrNull()
        }
    }
}