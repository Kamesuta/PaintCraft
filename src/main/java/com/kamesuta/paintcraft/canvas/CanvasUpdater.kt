package com.kamesuta.paintcraft.canvas

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.utility.MinecraftReflection
import com.kamesuta.paintcraft.PaintCraft
import com.kamesuta.paintcraft.map.MapBuffer
import com.kamesuta.paintcraft.map.mapSize
import com.kamesuta.paintcraft.util.UVIntArea
import org.bukkit.entity.Player
import org.bukkit.map.MapView
import java.lang.reflect.Array

/**
 * 地図の更新をクライアントに送信するためのクラス
 */
object CanvasUpdater {
    private val mapIconClass = MinecraftReflection.getMinecraftClass("MapIcon")
    private val mapIconArrayClass = MinecraftReflection.getArrayClass(mapIconClass)

    /**
     * プレイヤーにマップを送信する
     * @param player プレイヤー
     * @param mapView マップ
     * @param dirty 更新領域
     */
    fun sendMap(player: Player, mapView: MapView, buffer: MapBuffer, dirty: UVIntArea) {
        val part = createSubImage(buffer, dirty)
        val packet = createPacket(mapView, part, dirty)
        PaintCraft.instance.protocolManager.sendServerPacket(player, packet)
    }

    /**
     * 更新する領域を切り抜く
     * @param buffer マップのピクセルデータ
     * @param dirty 更新する領域
     */
    private fun createSubImage(
        buffer: MapBuffer,
        dirty: UVIntArea,
    ): MapBuffer {
        val width = dirty.width
        val height = dirty.height
        val part = MapBuffer(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                part[x + y * width] = buffer[dirty.p1.u + x + (dirty.p1.v + y) * mapSize]
            }
        }
        return part
    }

    /**
     * マップを更新するためのパケットを作成する
     * @param mapView マップビュー
     * @param part 更新する領域を切り抜いたマップのピクセルデータ
     * @param dirty 更新する領域
     */
    private fun createPacket(
        mapView: MapView,
        part: MapBuffer,
        dirty: UVIntArea,
    ): PacketContainer {
        // パケットを作成する
        val packet = PaintCraft.instance.protocolManager.createPacket(PacketType.Play.Server.MAP)

        // マップID
        packet.integers.write(0, mapView.id)
        // マップスケール
        packet.bytes.write(0, mapView.scale.value)
        // 位置を追跡するか (trackingPosition)
        packet.booleans.write(0, true)
        // 地図がロックされているか
        packet.booleans.write(1, mapView.isLocked)

        // アイコンの配列
        @Suppress("UNCHECKED_CAST")
        packet.getSpecificModifier(mapIconArrayClass as Class<Any>).write(0, Array.newInstance(mapIconClass, 0))

        // 更新する領域を設定する
        packet.integers.write(1, dirty.p1.u)
        packet.integers.write(2, dirty.p1.v)
        packet.integers.write(3, dirty.width)
        packet.integers.write(4, dirty.height)

        // 更新する領域のピクセルデータ
        packet.byteArrays.write(0, part)

        return packet
    }
}