package com.kamesuta.paintcraft.map

import com.kamesuta.paintcraft.PaintCraft
import com.kamesuta.paintcraft.util.ReflectionAccessor
import com.kamesuta.paintcraft.util.UVInt
import com.kamesuta.paintcraft.util.UVIntArea
import org.bukkit.entity.Player
import org.bukkit.map.MapCanvas
import org.bukkit.map.MapView

/**
 * 地図操作リフレクションクラス
 */
object MapReflection {
    /**
     * MapCanvasのピクセルデータを取得します
     * @param mapCanvas キャンバス
     * @return ピクセルデータ
     */
    fun getCanvasBuffer(mapCanvas: MapCanvas): MapBuffer? {
        return try {
            (ReflectionAccessor.getField(mapCanvas, "buffer") as ByteArray?)?.let {
                MapBuffer(it)
            }
        } catch (e: ReflectiveOperationException) {
            PaintCraft.instance.logger.warning("Failed to get MapCanvas buffer")
            null
        }
    }

    /**
     * MapViewのピクセルデータを設定します
     * @param mapView マップビュー
     * @return ピクセルデータ
     */
    fun getMapBuffer(mapView: MapView): MapBuffer? {
        return try {
            val worldMap: Any = ReflectionAccessor.getField(mapView, "worldMap")
                ?: return null
            val pixels = try {
                ReflectionAccessor.getField(worldMap, "colors") as ByteArray?
            } catch (e: NoSuchFieldException) {
                //Then we must be on 1.17
                ReflectionAccessor.getField(worldMap, "g") as ByteArray?
            }
            pixels?.let {
                MapBuffer(it)
            }
        } catch (e: ReflectiveOperationException) {
            PaintCraft.instance.logger.warning("Failed to get map buffer")
            null
        }
    }

    /**
     * キャンバスの更新領域を取得します
     * プレイヤーごとに更新領域が異なるため、player引数を指定して取得します
     * @param player プレイヤー
     * @param mapView マップビュー
     */
    fun getMapDirtyArea(player: Player, mapView: MapView): UVIntArea? {
        return try {
            val entity = ReflectionAccessor.invokeMethod(player, "getHandle")
                ?: return null
            val worldMap = ReflectionAccessor.getField(mapView, "worldMap")
                ?: return null
            val humanTrackerMap = ReflectionAccessor.getField(worldMap, "humans") as HashMap<*, *>?
                ?: return null
            val humanTracker = humanTrackerMap[entity]
                ?: return null
            val x1 = ReflectionAccessor.getField(humanTracker, "e") as Int
            val y1 = ReflectionAccessor.getField(humanTracker, "f") as Int
            val x2 = ReflectionAccessor.getField(humanTracker, "g") as Int
            val y2 = ReflectionAccessor.getField(humanTracker, "h") as Int
            UVIntArea(
                UVInt(x1, y1),
                UVInt(x2, y2),
            )
        } catch (e: ReflectiveOperationException) {
            PaintCraft.instance.logger.warning("Failed to get map dirty area for player ${player.name}")
            null
        }
    }
}
