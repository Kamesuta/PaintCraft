package com.kamesuta.paintcraft.map

import com.comphenix.protocol.utility.MinecraftReflection
import com.kamesuta.paintcraft.PaintCraft
import com.kamesuta.paintcraft.util.vec.Rect2i
import com.kamesuta.paintcraft.util.vec.Vec2i
import org.bukkit.entity.Player
import org.bukkit.map.MapCanvas
import org.bukkit.map.MapView
import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * 地図操作リフレクションクラス
 */
object DrawableMapReflection {
    /**
     * NMSにアクセスするためのクラス
     * NMSクラスが見つからなかったりした際、DrawableMapReflectionクラスの関数がそもそも呼べなくなるのを防ぐ
     */
    private object Accessor {
        // NMSクラス
        val worldMap: Class<*> = MinecraftReflection.getMinecraftClass("WorldMap")
        val humanTracker: Class<*> = MinecraftReflection.getMinecraftClass("WorldMap\$WorldMapHumanTracker")
        val craftEntity: Class<*> = MinecraftReflection.getCraftBukkitClass("entity.CraftEntity")
        val craftMapCanvas: Class<*> = MinecraftReflection.getCraftBukkitClass("map.CraftMapCanvas")
        val craftMapView: Class<*> = MinecraftReflection.getCraftBukkitClass("map.CraftMapView")

        // NMS関数/フィールド
        val craftMapCanvasBuffer: Field = craftMapCanvas.getDeclaredField("buffer").apply { isAccessible = true }
        val mapViewWorldMap: Field = craftMapView.getDeclaredField("worldMap").apply { isAccessible = true }
        val worldMapColors: Field = worldMap
            .let { field ->
                // 1.17まではcolorsに入っている
                runCatching { field.getDeclaredField("colors") }
                    // 1.17からはgに入っている
                    .recover { field.getDeclaredField("g") }
                    .getOrThrow()
            }.apply { isAccessible = true }
        val craftEntityGetHandle: Method = craftEntity.getDeclaredMethod("getHandle").apply { isAccessible = true }
        val worldMapHumans: Field = worldMap.getDeclaredField("humans").apply { isAccessible = true }
        val humanTrackerMinX: Field = humanTracker.getDeclaredField("e").apply { isAccessible = true }
        val humanTrackerMinY: Field = humanTracker.getDeclaredField("f").apply { isAccessible = true }
        val humanTrackerMaxX: Field = humanTracker.getDeclaredField("g").apply { isAccessible = true }
        val humanTrackerMaxY: Field = humanTracker.getDeclaredField("h").apply { isAccessible = true }
    }

    /**
     * NMSクラスが存在するかチェックします
     * 存在しない場合は例外を投げます
     */
    @Throws(ReflectiveOperationException::class)
    fun checkReflection() {
        try {
            // NMSクラスが見つからなかったらエラー
            Accessor.javaClass
        } catch (e: ExceptionInInitializerError) {
            // 中身を返す
            throw e.cause ?: e
        }
    }

    /**
     * MapCanvasのピクセルデータを取得します
     * @param mapCanvas キャンバス
     * @return ピクセルデータ
     */
    fun getCanvasBuffer(mapCanvas: MapCanvas): DrawableMapBuffer? {
        return runCatching {
            (Accessor.craftMapCanvasBuffer[mapCanvas] as ByteArray?)?.let {
                DrawableMapBuffer(it)
            }
        }.onFailure {
            PaintCraft.instance.logger.warning("Failed to get MapCanvas buffer")
        }.getOrNull()
    }

    /**
     * MapViewのピクセルデータを設定します
     * @param mapView マップビュー
     * @return ピクセルデータ
     */
    fun getMapBuffer(mapView: MapView): DrawableMapBuffer? {
        return runCatching {
            val worldMap: Any = Accessor.mapViewWorldMap[mapView]
                ?: return null
            (Accessor.worldMapColors[worldMap] as ByteArray?)?.let {
                DrawableMapBuffer(it)
            }
        }.onFailure {
            PaintCraft.instance.logger.warning("Failed to get map buffer")
        }.getOrNull()
    }

    /**
     * キャンバスの更新領域を取得します
     * プレイヤーごとに更新領域が異なるため、player引数を指定して取得します
     * @param player プレイヤー
     * @param mapView マップビュー
     */
    fun getMapDirtyArea(player: Player, mapView: MapView): Rect2i? {
        return runCatching {
            val handle = Accessor.craftEntityGetHandle(player)
                ?: return null
            val worldMap = Accessor.mapViewWorldMap[mapView]
                ?: return null
            val humanTrackerMap = Accessor.worldMapHumans[worldMap] as HashMap<*, *>?
                ?: return null
            val humanTracker = humanTrackerMap[handle]
                ?: return null
            val x1 = Accessor.humanTrackerMinX[humanTracker] as Int
            val y1 = Accessor.humanTrackerMinY[humanTracker] as Int
            val x2 = Accessor.humanTrackerMaxX[humanTracker] as Int
            val y2 = Accessor.humanTrackerMaxY[humanTracker] as Int
            Rect2i(
                Vec2i(x1, y1),
                Vec2i(x2, y2),
            )
        }.onFailure {
            PaintCraft.instance.logger.warning("Failed to get map dirty area for player ${player.name}")
        }.getOrNull()
    }
}
