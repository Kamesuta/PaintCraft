package com.kamesuta.paintcraft.map

import com.comphenix.protocol.utility.MinecraftReflection
import com.kamesuta.paintcraft.PaintCraft
import com.kamesuta.paintcraft.util.vec.Rect2i
import com.kamesuta.paintcraft.util.vec.Vec2i
import org.bukkit.entity.Player
import org.bukkit.map.MapCanvas
import org.bukkit.map.MapRenderer
import org.bukkit.map.MapView
import java.lang.reflect.Constructor
import java.lang.reflect.Field

/**
 * 地図操作リフレクションクラス
 */
object DrawableMapReflection {
    /**
     * NMSにアクセスするためのクラス
     * NMSクラスが見つからなかったりした際、クラスの関数がそもそも呼べなくなるのを防ぐ
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
        val worldMapHumans: Field = worldMap.getDeclaredField("humans").apply { isAccessible = true }
        val humanTrackerIsDirty: Field = humanTracker.getDeclaredField("d").apply { isAccessible = true }
        val humanTrackerMinX: Field = humanTracker.getDeclaredField("e").apply { isAccessible = true }
        val humanTrackerMinY: Field = humanTracker.getDeclaredField("f").apply { isAccessible = true }
        val humanTrackerMaxX: Field = humanTracker.getDeclaredField("g").apply { isAccessible = true }
        val humanTrackerMaxY: Field = humanTracker.getDeclaredField("h").apply { isAccessible = true }
        val craftMapCanvasNew: Constructor<*> =
            craftMapCanvas.getDeclaredConstructor(craftMapView).apply { isAccessible = true }
        val craftMapViewCanvases: Field = craftMapView.getDeclaredField("canvases").apply { isAccessible = true }
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
    fun getCanvasBuffer(mapCanvas: MapCanvas): ByteArray? {
        return runCatching {
            Accessor.craftMapCanvasBuffer[mapCanvas] as ByteArray?
        }.onFailure {
            PaintCraft.instance.logger.warning("Failed to get MapCanvas buffer")
        }.getOrNull()
    }

    /**
     * MapViewのピクセルデータを設定します
     * @param mapView マップビュー
     * @return ピクセルデータ
     */
    fun getMapBuffer(mapView: MapView): ByteArray? {
        return runCatching {
            val worldMap: Any = Accessor.mapViewWorldMap[mapView]
                ?: return null
            Accessor.worldMapColors[worldMap] as ByteArray?
        }.onFailure {
            PaintCraft.instance.logger.warning("Failed to get map buffer")
        }.getOrNull()
    }

    /**
     * キャンバスの更新領域を取得します
     * プレイヤーごとに更新領域が異なるため、player引数を指定して取得します
     * @param mapView マップビュー
     */
    fun getMapDirtyArea(mapView: MapView): List<Pair<Player, Rect2i>>? {
        return runCatching {
            val worldMap = Accessor.mapViewWorldMap[mapView]
                ?: return null
            val humanTrackerMap = Accessor.worldMapHumans[worldMap] as HashMap<*, *>?
                ?: return null
            return humanTrackerMap
                .mapNotNull { (human, humanTracker) ->
                    // プレイヤーを取得
                    val player = MinecraftReflection.getBukkitEntity(human) as? Player
                        ?: return@mapNotNull null
                    // 変更があるか
                    val isDirty = Accessor.humanTrackerIsDirty[humanTracker] as Boolean
                    if (!isDirty) {
                        // 変更箇所なし
                        return@mapNotNull null
                    }
                    // 変更箇所を取得
                    val x1 = Accessor.humanTrackerMinX[humanTracker] as Int
                    val y1 = Accessor.humanTrackerMinY[humanTracker] as Int
                    val x2 = Accessor.humanTrackerMaxX[humanTracker] as Int
                    val y2 = Accessor.humanTrackerMaxY[humanTracker] as Int
                    val dirty = Rect2i(
                        Vec2i(x1, y1),
                        Vec2i(x2, y2),
                    )
                    player to dirty
                }
        }.onFailure {
            PaintCraft.instance.logger.warning("Failed to get map dirty area")
        }.getOrNull()
    }

    /**
     * MapViewからMapCanvasを取得します
     * MapCanvasはrender関数で取れますが、プラグイン読み込み直後の描き込みなど、
     * MapView#renderが呼ばれるよりも早いタイミングでMapCanvasがほしいタイミングがあるためリフレクションで取得します。
     * @param mapView マップビュー
     * @param renderer レンダラー
     */
    fun createAndPutCanvas(mapView: MapView, renderer: MapRenderer): MapCanvas? {
        return runCatching {
            @Suppress("UNCHECKED_CAST")
            val canvases = Accessor.craftMapViewCanvases[mapView] as MutableMap<MapRenderer, MutableMap<Any?, Any?>>
            val canvasMap = canvases[renderer]
                ?: throw IllegalStateException("Craft canvas map not found")
            canvasMap.computeIfAbsent(null) {
                Accessor.craftMapCanvasNew.newInstance(mapView)
            } as MapCanvas
        }.onFailure {
            PaintCraft.instance.logger.warning("Failed to get map craft canvas")
        }.getOrNull()
    }
}
