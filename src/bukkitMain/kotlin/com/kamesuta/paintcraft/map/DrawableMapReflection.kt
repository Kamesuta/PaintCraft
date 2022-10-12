package com.kamesuta.paintcraft.map

import com.comphenix.protocol.utility.MinecraftReflection
import com.kamesuta.paintcraft.PaintCraft
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
     * NMSクラスが見つからなかったりした際、クラスの関数がそもそも呼べなくなるのを防ぐ
     */
    private object Accessor {
        // NMSクラス
        val worldMap: Class<*> = MinecraftReflection.getMinecraftClass("WorldMap")
        val craftMapCanvas: Class<*> = MinecraftReflection.getCraftBukkitClass("map.CraftMapCanvas")
        val craftMapView: Class<*> = MinecraftReflection.getCraftBukkitClass("map.CraftMapView")
        val enumProtocol: Class<*> = MinecraftReflection.getEnumProtocolClass()
        val enumProtocolPacketList: Class<*> = enumProtocol.declaredClasses.first { it.simpleName == "a" }
        val enumProtocolDirection: Class<*> = MinecraftReflection.getMinecraftClass("EnumProtocolDirection")
        val packetPlayOutMap: Class<*> = MinecraftReflection.getMinecraftClass("PacketPlayOutMap")

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
        val worldMapFlagDirty: Method =
            worldMap.getDeclaredMethod("flagDirty", Int::class.java, Int::class.java).apply { isAccessible = true }
        val worldMapHumans: Field = worldMap.getDeclaredField("humans").apply { isAccessible = true }
        val enumProtocolPlay: Field = enumProtocol.getDeclaredField("PLAY").apply { isAccessible = true }
        val enumProtocolPacketDirectionMap: Field = enumProtocol.getDeclaredField("h").apply { isAccessible = true }
        val enumProtocolPacketListGetPacketId: Method =
            enumProtocolPacketList.getDeclaredMethod("a", Class::class.java).apply { isAccessible = true }
        val enumProtocolDirectionClientBound: Field =
            enumProtocolDirection.getDeclaredField("CLIENTBOUND").apply { isAccessible = true }
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
     * 更新フラグを立てます
     * @param mapView マップビュー
     * @param x X座標
     * @param y Y座標
     */
    fun flagDirty(mapView: MapView, x: Int, y: Int) {
        runCatching {
            val worldMap: Any = Accessor.mapViewWorldMap[mapView]
                ?: return
            Accessor.worldMapFlagDirty(worldMap, x, y)
        }.onFailure {
            PaintCraft.instance.logger.warning("Failed to set dirty flag")
        }
    }

    /**
     * キャンバスを見ているプレイヤーを取得します
     * @param mapView マップビュー
     * @return プレイヤーリスト
     */
    fun getMapTrackingPlayers(mapView: MapView): List<Player>? {
        return runCatching {
            val worldMap = Accessor.mapViewWorldMap[mapView]
                ?: return null
            val humanTrackerMap = Accessor.worldMapHumans[worldMap] as HashMap<*, *>?
                ?: return null
            return humanTrackerMap
                .mapNotNull { (human, _) ->
                    // プレイヤーを取得
                    MinecraftReflection.getBukkitEntity(human) as? Player
                }
        }.onFailure {
            PaintCraft.instance.logger.warning("Failed to get map tracking players")
        }.getOrNull()
    }

    /**
     * PacketPlayOutMapのパケットIDを取得します
     * @return パケットID
     */
    fun getPacketPlayOutMapId(): Int {
        return runCatching {
            val playEnum = Accessor.enumProtocolPlay[null]
            val directionClientBound = Accessor.enumProtocolDirectionClientBound[null]
            val directionMap = Accessor.enumProtocolPacketDirectionMap[playEnum] as Map<*, *>
            val packetList = directionMap[directionClientBound]
            Accessor.enumProtocolPacketListGetPacketId(packetList, Accessor.packetPlayOutMap) as Int
        }.onFailure {
            PaintCraft.instance.logger.warning("Failed to get packet id")
        }.getOrNull() ?: -1
    }
}
