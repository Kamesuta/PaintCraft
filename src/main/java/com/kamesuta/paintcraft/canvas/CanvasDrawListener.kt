package com.kamesuta.paintcraft.canvas

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.kamesuta.paintcraft.PaintCraft
import com.kamesuta.paintcraft.map.DrawableMapItem
import com.kamesuta.paintcraft.util.DebugLocationType
import com.kamesuta.paintcraft.util.DebugLocationVisualizer.clearDebugLocation
import com.kamesuta.paintcraft.util.LocationOperation
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent

/**
 * 描画用のイベントリスナー
 */
class CanvasDrawListener : Listener {
    /**
     * アイテムフレームを直接左クリックしたときに描画する
     * @param event イベント
     */
    @EventHandler
    fun onAttack(event: EntityDamageByEntityEvent) {
        // 左クリックしたプレイヤー
        val player = event.damager as? Player
            ?: return
        // プレイヤーの右手にインクがあるか
        if (player.hasPencil()) {
            return
        }
        // デバッグ座標を初期化
        player.clearDebug()

        // キャンバス以外は無視
        if (!event.entity.isCanvas()) {
            return
        }
        // イベントをキャンセル (ここに来た時点でキャンバスを左クリックしているのでアイテムフレームが破壊されないようにキャンセルする)
        event.isCancelled = true

        // キャンバスのセッションを取得
        val session = CanvasSessionManager.getSession(player)

        // レイツールを初期化
        val rayTrace = CanvasRayTrace(player)
        // レイを飛ばしてチェックしアイテムフレームを取得
        // (イベントからエンティティが取れるが、前後関係でより近い額縁がある事があるのでレイを飛ばしてエンティティを取得する)
        val ray = rayTrace.rayTraceCanvas(session.eyeLocation)
            ?: return

        // 裏からのクリックは無視
        if (!rayTrace.isCanvasFrontSide(session.eyeLocation.direction, ray.itemFrame)) {
            return
        }

        // キャンバスに描画
        rayTrace.manipulate(
            ray,
            session,
            CanvasActionType.LEFT_CLICK
        )
    }

    /**
     * アイテムフレームを直接右クリックしたときに描画する
     * @param event イベント
     */
    @EventHandler
    fun onInteractEntity(event: PlayerInteractEntityEvent) {
        // 右クリックしたプレイヤー
        val player = event.player
        // プレイヤーの右手にインクがあるか
        if (player.hasPencil()) {
            return
        }
        // デバッグ座標を初期化
        player.clearDebug()

        // キャンバス以外は無視
        if (!event.rightClicked.isCanvas()) {
            return
        }
        // イベントをキャンセル (ここに来た時点でキャンバスを右クリックしているのでアイテムフレームが回転しないようにキャンセルする)
        event.isCancelled = true

        // キャンバスのセッションを取得
        val session = CanvasSessionManager.getSession(player)

        // レイツールを初期化
        val rayTrace = CanvasRayTrace(player)
        // レイを飛ばしてチェックしアイテムフレームを取得
        // (イベントからエンティティが取れるが、前後関係でより近い額縁がある事があるのでレイを飛ばしてエンティティを取得する)
        val ray = rayTrace.rayTraceCanvas(session.eyeLocation)
            ?: return

        // 裏からのクリックは無視
        if (!rayTrace.isCanvasFrontSide(session.eyeLocation.direction, ray.itemFrame)) {
            return
        }

        // キャンバスに描画
        rayTrace.manipulate(
            ray,
            session,
            CanvasActionType.RIGHT_CLICK
        )
    }

    /**
     * アイテムフレームを遠くから左右クリックしたときに描画する
     * @param event イベント
     */
    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        // 左右クリックしたプレイヤー
        val player = event.player
        // プレイヤーの右手にインクがあるか
        if (player.hasPencil()) {
            return
        }
        // デバッグ座標を初期化
        player.clearDebug()

        // キャンバスのセッションを取得
        val session = CanvasSessionManager.getSession(player)

        // レイツールを初期化
        val rayTrace = CanvasRayTrace(player)
        // レイを飛ばしてアイテムフレームを取得
        val ray = rayTrace.rayTraceCanvas(session.eyeLocation)
            ?: return

        // イベントをキャンセル
        event.isCancelled = true

        // 裏からのクリックは無視
        if (!rayTrace.isCanvasFrontSide(session.eyeLocation.direction, ray.itemFrame)) {
            return
        }

        // キャンバスに描画
        rayTrace.manipulate(
            ray, session,
            when (event.action) {
                Action.RIGHT_CLICK_BLOCK -> CanvasActionType.RIGHT_CLICK
                Action.RIGHT_CLICK_AIR -> CanvasActionType.RIGHT_CLICK
                Action.LEFT_CLICK_BLOCK -> CanvasActionType.LEFT_CLICK
                Action.LEFT_CLICK_AIR -> return // 右クリックを誤検知することがあるため無視
                else -> return
            }
        )
    }

    /**
     * プレイヤーの移動パケットリスナーを作成する
     * @return プレイヤーの移動パケットリスナー
     */
    fun createMovePacketAdapter(): PacketAdapter {
        return object : PacketAdapter(
            PaintCraft.instance,
            ListenerPriority.NORMAL,
            PacketType.Play.Client.LOOK,
            PacketType.Play.Client.POSITION,
            PacketType.Play.Client.POSITION_LOOK,
        ) {
            override fun onPacketReceiving(event: PacketEvent) {
                val player = event.player
                val packet = event.packet
                val x = packet.doubles.read(0)
                val y = packet.doubles.read(1)
                val z = packet.doubles.read(2)
                val yaw = packet.float.read(0)
                val pitch = packet.float.read(1)
                val location = Location(player.world, x, y + player.eyeHeight, z, yaw, pitch)
                onMovePacket(
                    player,
                    location,
                    when (event.packetType) {
                        PacketType.Play.Client.LOOK -> LocationOperation.LOOK
                        PacketType.Play.Client.POSITION -> LocationOperation.POSITION
                        PacketType.Play.Client.POSITION_LOOK -> LocationOperation.POSITION_LOOK
                        else -> LocationOperation.NONE
                    }
                )
            }
        }
    }

    /**
     * 動いたとき (描いている最中のみ)
     * PaintCraftクラス(ProtocolLib)から呼ばれる
     * @param player プレイヤー
     * @param eyeLocation 移動先の位置
     * @param locationOperation 移動先の位置における操作
     */
    private fun onMovePacket(player: Player, eyeLocation: Location, locationOperation: LocationOperation) {
        // プレイヤーの右手にインクがあるか
        if (player.hasPencil()) {
            return
        }
        // キャンバスのセッションを取得
        val session = CanvasSessionManager.getSession(player)

        // パケットの座標を合成しプレイヤーの座標と目線を計算
        val playerEyePos = locationOperation.operation(session.eyeLocation, eyeLocation)
        // 目線の座標を更新
        session.eyeLocation = playerEyePos

        // キャンバスが描画中かどうかを確認
        if (!session.tool.isDrawing) {
            return
        }

        // メインスレッド以外でエンティティを取得できないため、メインスレッドで処理
        Bukkit.getScheduler().runTask(PaintCraft.instance) { ->
            // スレッドが違うと問題が起こるためここでclear
            // デバッグ座標を初期化
            player.clearDebug()

            // レイツールを初期化
            val rayTrace = CanvasRayTrace(player)
            // レイを飛ばしてアイテムフレームを取得
            val ray = rayTrace.rayTraceCanvas(playerEyePos)
                ?: return@runTask

            // 裏からのクリックは無視
            if (!rayTrace.isCanvasFrontSide(playerEyePos.direction, ray.itemFrame)) {
                return@runTask
            }

            // キャンバスに描画
            rayTrace.manipulate(
                ray,
                session,
                CanvasActionType.MOUSE_MOVE
            )
        }
    }

    /**
     * キャンバスかどうか判定
     */
    private fun Entity.isCanvas(): Boolean {
        // アイテムフレームを取得
        val itemFrame = this as? ItemFrame
            ?: return false
        // アイテムが地図かどうかを確認
        if (itemFrame.item.type != Material.FILLED_MAP) {
            return false
        }
        // キャンバスか判定し取得
        DrawableMapItem.get(itemFrame.item)
            ?: return false
        return true
    }

    /**
     * デバッグ座標を初期化
     */
    private fun Player.clearDebug() = clearDebugLocation(DebugLocationType.DebugLocationGroup.CANVAS_DRAW)

    /**
     * プレイヤーがペンを持っているかどうかを確認する
     * @return ペンを持っているかどうか
     */
    private fun Player.hasPencil() = inventory.itemInMainHand.type != Material.INK_SAC
}