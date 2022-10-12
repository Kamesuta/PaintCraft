package com.kamesuta.paintcraft.map

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.utility.MinecraftReflection
import com.kamesuta.paintcraft.PaintCraft
import com.kamesuta.paintcraft.map.image.PixelImageBuffer
import com.kamesuta.paintcraft.util.vec.Rect2i
import org.bukkit.entity.Player
import org.bukkit.map.MapView
import java.lang.reflect.Array

/**
 * 地図の更新をクライアントに送信するためのクラス
 */
class DrawableMapUpdater {
    /** キャンバス更新のキャッシュ */
    private val canvasCache = PixelImageCacheBuffer()

    /** 送信するパケット */
    private var canvasPacket: PacketContainer? = null


    /**
     * プレイヤーにマップを送信する
     * @param player プレイヤー
     */
    fun sendMap(player: Player) {
        if (canvasPacket != null) {
            PaintCraft.instance.protocolManager.sendServerPacket(player, canvasPacket)
        }
    }

    /**
     * マップを更新するためのパケットを作成する
     * @param mapView マップビュー
     * @param buffer ピクセルデータ
     * @param dirty 更新する領域
     */
    fun createPacket(
        mapView: MapView,
        buffer: PixelImageBuffer,
        dirty: Rect2i,
    ) {
        // 更新する領域を切り出す
        canvasCache.subImage(buffer, dirty)

        // パケットを作成する
        val packet = PaintCraft.instance.protocolManager.createPacket(PacketType.Play.Server.MAP)

        // マップID
        packet.integers.write(0, mapView.id)
        // マップスケール
        @Suppress("DEPRECATION")
        packet.bytes.write(0, mapView.scale.value)
        // 位置を追跡するか (trackingPosition)
        packet.booleans.write(0, true)
        // 地図がロックされているか
        packet.booleans.write(1, mapView.isLocked)

        // アイコンの配列
        packet.getSpecificModifier(mapIconArrayClass).write(0, Array.newInstance(mapIconClass, 0))

        // 更新する領域を設定する
        packet.integers.write(1, dirty.min.x)
        packet.integers.write(2, dirty.min.y)
        packet.integers.write(3, dirty.width)
        packet.integers.write(4, dirty.height)

        // 更新する領域のピクセルデータ
        packet.byteArrays.write(0, canvasCache.pixels.copyOf(dirty.width * dirty.height))

        canvasPacket = packet
    }


        // 更新する領域のピクセルデータ

    companion object {
        private val mapIconClass = MinecraftReflection.getMinecraftClass("MapIcon")

        @Suppress("UNCHECKED_CAST")
        private val mapIconArrayClass = MinecraftReflection.getArrayClass(mapIconClass) as Class<Any>

        return packet
    }
}