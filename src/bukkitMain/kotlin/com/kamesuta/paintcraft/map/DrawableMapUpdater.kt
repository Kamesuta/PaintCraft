package com.kamesuta.paintcraft.map

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.injector.netty.WirePacket
import com.kamesuta.paintcraft.PaintCraft
import com.kamesuta.paintcraft.map.image.PixelImageBuffer
import com.kamesuta.paintcraft.map.image.PixelImageCacheBuffer
import com.kamesuta.paintcraft.util.vec.Rect2i
import io.netty.buffer.ByteBuf
import org.bukkit.entity.Player
import org.bukkit.map.MapView

/**
 * 地図の更新をクライアントに送信するためのクラス
 */
class DrawableMapUpdater {
    /** キャンバス更新のキャッシュ */
    private val canvasCache = PixelImageCacheBuffer()

    /** 送信するパケット */
    private var canvasPacket: WirePacket? = null

    /**
     * プレイヤーにマップを送信する
     * @param player プレイヤー
     */
    fun sendMap(player: Player) {
        if (canvasPacket != null) {
            PaintCraft.instance.protocolManager.sendWirePacket(
                player,
                canvasPacket
            )
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
        canvasPacket = object : WirePacket(PacketType.Play.Server.MAP, ZeroByte) {
            override fun writeFully(buf: ByteBuf) {
                // パケットID
                writeVarInt(buf, packetPlayOutMapId)

                // マップID
                writeVarInt(buf, mapView.id)
                // マップスケール
                @Suppress("DEPRECATION")
                buf.writeByte(mapView.scale.value.toInt())
                // 位置を追跡するか (trackingPosition)
                buf.writeBoolean(true)
                // 地図がロックされているか
                buf.writeBoolean(mapView.isLocked)

                // アイコンの配列 (0個)
                writeVarInt(buf, 0)

                // 更新する領域を設定する
                val width = dirty.width
                buf.writeByte(width)
                if (width > 0) {
                    buf.writeByte(dirty.height)
                    buf.writeByte(dirty.min.x)
                    buf.writeByte(dirty.min.y)

                    // 更新する領域のピクセルデータ
                    writeVarInt(buf, dirty.width * dirty.height)
                    buf.writeBytes(canvasCache.pixels, 0, dirty.width * dirty.height)
                }
            }
        }
    }

    companion object {
        /** マップのパケットID */
        val packetPlayOutMapId = DrawableMapReflection.getPacketPlayOutMapId()

        /** 空の配列 */
        private val ZeroByte = ByteArray(0)
    }
}