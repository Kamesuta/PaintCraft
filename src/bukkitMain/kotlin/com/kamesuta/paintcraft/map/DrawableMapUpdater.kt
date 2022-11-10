package com.kamesuta.paintcraft.map

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.injector.netty.WirePacket
import com.kamesuta.paintcraft.PaintCraft
import com.kamesuta.paintcraft.map.image.PixelImageCacheBuffer
import io.netty.buffer.ByteBuf
import org.bukkit.entity.Player
import org.bukkit.map.MapView

/**
 * 地図の更新をクライアントに送信するためのクラス
 */
class DrawableMapUpdater {
    /** 更新するマップビュー */
    private var canvasMapView: MapView? = null

    /** キャンバス更新のキャッシュ */
    private var canvasCache: PixelImageCacheBuffer? = null

    /** 送信するパケット */
    private var canvasPacket: WirePacket = object : WirePacket(PacketType.Play.Server.MAP, ZeroByte) {
        override fun writeFully(buf: ByteBuf) {
            // マップビューと更新領域がある場合のみ送信
            val mapView = canvasMapView
                ?: return
            val cache = canvasCache
                ?: return
            val dirty = cache.dirty.rect
                ?: return

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
                buf.writeBytes(cache.pixels, 0, dirty.width * dirty.height)
            }
        }
    }

    /**
     * プレイヤーにマップを送信する
     * @param player プレイヤー
     */
    fun sendMap(player: Player, isPriority: Boolean = false) {
        // マップビューと更新領域がある場合のみ送信
        canvasMapView ?: return
        canvasCache ?: return

        // 高頻度でパケットを送信するとパイプが詰まってマイクラのパケットが遅延するので、キューが空のときのみ送信することで回避する。
        // 優先度が高い場合はキューが空でなくても送信する。
        if (!isPriority) {
            // イベントループにキューが溜まっている場合は送信しない
            val eventLoop = DrawableMapUpdaterReflection.getEventLoop(player)
            if (eventLoop != null && eventLoop.pendingTasks() > 1) {
                //PaintCraft.instance.logger.warning("skip: ${player.name}, pendingTasks: ${eventLoop.pendingTasks()}")
                return
            }
        }

        // パケットを送信する
        PaintCraft.instance.protocolManager.sendWirePacket(
            player,
            canvasPacket
        )
    }

    /**
     * マップを更新するためのパケットを作成する
     * @param mapView マップビュー
     * @param buffer ピクセルデータ
     */
    fun createPacket(
        mapView: MapView,
        buffer: PixelImageCacheBuffer,
    ) {
        // パケットを作成する
        canvasMapView = mapView
        canvasCache = buffer
    }

    companion object {
        /** マップのパケットID */
        private val packetPlayOutMapId = DrawableMapReflection.getPacketPlayOutMapId()

        /** 空の配列 */
        private val ZeroByte = ByteArray(0)
    }
}