package com.kamesuta.paintcraft.map

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.utility.MinecraftReflection
import com.kamesuta.paintcraft.PaintCraft
import com.kamesuta.paintcraft.util.vec.Rect2i
import org.bukkit.entity.Player
import org.bukkit.map.MapView
import java.lang.reflect.Array

/**
 * 地図の更新をクライアントに送信するためのクラス
 */
object DrawableMapUpdater {
    private val mapIconClass = MinecraftReflection.getMinecraftClass("MapIcon")

    @Suppress("UNCHECKED_CAST")
    private val mapIconArrayClass = MinecraftReflection.getArrayClass(mapIconClass) as Class<Any>

    /**
     * プレイヤーにマップを送信する
     * @param player プレイヤー
     * @param mapView マップ
     * @param dirty 更新領域
     */
    fun sendMap(player: Player, mapView: MapView, buffer: DrawableMapBuffer, dirty: Rect2i) {
        val part = buffer.createSubImage(dirty)
        val packet = createPacket(mapView, part, dirty)
        PaintCraft.instance.protocolManager.sendServerPacket(player, packet)
    }

    /**
     * マップを更新するためのパケットを作成する
     * @param mapView マップビュー
     * @param part 更新する領域を切り抜いたマップのピクセルデータ
     * @param dirty 更新する領域
     */
    private fun createPacket(
        mapView: MapView,
        part: ByteArray,
        dirty: Rect2i,
    ): PacketContainer {
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
        packet.integers.write(1, dirty.p1.x)
        packet.integers.write(2, dirty.p1.y)
        packet.integers.write(3, dirty.width)
        packet.integers.write(4, dirty.height)

        // 更新する領域のピクセルデータ
        packet.byteArrays.write(0, part)

        return packet
    }
}