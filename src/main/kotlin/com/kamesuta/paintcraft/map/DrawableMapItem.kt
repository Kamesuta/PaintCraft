package com.kamesuta.paintcraft.map

import com.kamesuta.paintcraft.map.behavior.DrawBehaviorTypes
import com.kamesuta.paintcraft.map.draw.Drawable
import com.kamesuta.paintcraft.util.PersistentDataProperty
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.map.MapView

/**
 * 書き込み可能マップ
 * @param itemStack アイテム
 * @param mapView マップ
 * @param renderer レンダラー
 */
class DrawableMapItem(
    val itemStack: ItemStack,
    val mapView: MapView,
    val renderer: DrawableMapRenderer,
) {
    /**
     * マップに描画する
     * @param f 描画する関数
     */
    fun draw(f: Drawable.() -> Unit) {
        renderer.behavior.draw(f)
    }

    companion object {
        /**
         * アイテムのビヘイビア、マップとして認識するためのキー
         */
        private val PAINT_BEHAVIOR = PersistentDataProperty.string("type")

        /**
         * アイテムのグループID
         */
        private val PAINT_GROUP_ID = PersistentDataProperty.integer("group_id")

        /**
         * 書き込み可能マップを作成する
         * @param world ワールド
         */
        fun create(world: World, type: DrawBehaviorTypes.Desc): DrawableMapItem {
            // マップを作成する
            val mapView = Bukkit.getServer().createMap(world)
            // レンダラーを初期化
            val renderer = DrawableMapRenderer(type)
            // レンダラーを設定
            mapView.setRenderer(renderer)

            // マップの設定
            mapView.centerX = -999999
            mapView.centerZ = -999999
            mapView.scale = MapView.Scale.FARTHEST
            // アイテムの作成
            val item = ItemStack(Material.FILLED_MAP)
            item.editMeta {
                it as MapMeta
                it.mapView = mapView
                PAINT_BEHAVIOR[it.persistentDataContainer] = type.name
                PAINT_GROUP_ID[it.persistentDataContainer] = 1
            }
            // 初回描画
            renderer.behavior.init()
            // インスタンスを作成
            return DrawableMapItem(item, mapView, renderer)
        }

        /**
         * 書き込み可能マップであれば取得する
         * @param item アイテム
         * @return 書き込み可能マップ
         */
        fun get(item: ItemStack): DrawableMapItem? {
            // 地図でなければ無視
            if (item.type != Material.FILLED_MAP)
                return null
            // メタデータからマップを取得
            val itemMeta = item.itemMeta
            val mapView = (itemMeta as? MapMeta)?.mapView
                ?: return null
            // レンダラーを取得
            val drawableMapRenderer = mapView.getRenderer()
            val renderer = if (drawableMapRenderer != null) {
                // レンダラーがあれば使用
                drawableMapRenderer
            } else {
                // 描きこむツール
                val type = DrawBehaviorTypes.types[PAINT_BEHAVIOR[itemMeta.persistentDataContainer]]
                    ?: return null
                // レンダラーを初期化
                val renderer = DrawableMapRenderer(type)
                // レンダラーを設定
                mapView.setRenderer(renderer)
                // 初回描画
                renderer.behavior.init()
                renderer
            }
            // インスタンスを作成
            return DrawableMapItem(item, mapView, renderer)
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