package com.kamesuta.paintcraft.frame

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.utility.MinecraftReflection
import com.kamesuta.paintcraft.PaintCraft
import com.kamesuta.paintcraft.canvas.CanvasActionType
import com.kamesuta.paintcraft.canvas.CanvasSession
import com.kamesuta.paintcraft.canvas.CanvasSessionManager
import com.kamesuta.paintcraft.map.DrawableMapItem
import com.kamesuta.paintcraft.util.DebugLocationType
import com.kamesuta.paintcraft.util.DebugLocationVisualizer.clearDebugLocation
import com.kamesuta.paintcraft.util.LocationOperation
import com.kamesuta.paintcraft.util.TimeWatcher
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.event.Listener

/**
 * 描画用のイベントリスナー
 */
class FrameDrawListener : Listener, Runnable {
    /**
     * ティックイベント
     */
    override fun run() {
        // トロッコなど乗っていた場合はティックイベントで動かす
        for (player in Bukkit.getOnlinePlayers()) {
            // プレイヤーの右手にインクがあるか
            if (player.hasPencil()) {
                continue
            }

            // パケットを処理
            onMovePacket(player, player.eyeLocation, LocationOperation.POSITION, true)
        }
    }

    /**
     * プレイヤーの移動パケットリスナーを作成する
     * @return プレイヤーの移動パケットリスナー
     */
    fun createMovePacketAdapter(): PacketAdapter {
        return object : PacketAdapter(
            PaintCraft.instance,
            ListenerPriority.NORMAL,
            listOf(
                PacketType.Play.Client.LOOK,
                PacketType.Play.Client.POSITION,
                PacketType.Play.Client.POSITION_LOOK,
                PacketType.Play.Client.VEHICLE_MOVE,
            ),
        ) {
            /** 受信 (クライアント→サーバー) */
            override fun onPacketReceiving(event: PacketEvent) {
                // プレイヤーがいなければ無視
                val player = event.player
                    ?: return
                // プレイヤーの右手にインクがあるか
                if (player.hasPencil()) {
                    return
                }

                // パケット解析
                val packet = event.packet
                // 座標を読み取る
                val x = packet.doubles.read(0)
                val y = packet.doubles.read(1)
                val z = packet.doubles.read(2)
                val yaw = packet.float.read(0)
                val pitch = packet.float.read(1)

                // 座標構築
                val location = Location(player.world, x, y + player.eyeHeight, z, yaw, pitch)
                // 更新する部分の指定
                val locationOperation = when (event.packetType) {
                    PacketType.Play.Client.LOOK -> LocationOperation.LOOK
                    PacketType.Play.Client.POSITION -> LocationOperation.POSITION
                    PacketType.Play.Client.POSITION_LOOK -> LocationOperation.POSITION_LOOK
                    PacketType.Play.Client.VEHICLE_MOVE -> {
                        // 乗り物のyOffset
                        val yOffset = player.vehicle?.let {
                            FrameReflection.getYOffset(player) + FrameReflection.getMountedYOffset(it)
                        } ?: 0.0
                        location.add(0.0, yOffset, 0.0)
                        LocationOperation.POSITION
                    }

                    else -> LocationOperation.NONE
                }

                // メインスレッド以外でエンティティを取得できないため、メインスレッドで処理
                Bukkit.getScheduler().runTask(PaintCraft.instance) { ->
                    // パケットを処理
                    onMovePacket(player, location, locationOperation, false)
                }
            }
        }
    }

    /**
     * 動いたとき
     * @param player プレイヤー
     * @param eyeLocation 移動先の位置
     * @param locationOperation 移動先の位置における操作
     * @param isTickEvent ティックイベントか
     */
    private fun onMovePacket(
        player: Player,
        eyeLocation: Location,
        locationOperation: LocationOperation,
        isTickEvent: Boolean
    ) {
        // キャンバスのセッションを取得
        val session = CanvasSessionManager.getSession(player)

        // ティックイベントかどうか
        if (isTickEvent) {
            // ティックイベントの場合、乗り物に乗っていないときは無視
            if (!player.isInsideVehicle) {
                return
            }

            // 直前でクライアント側の移動パケットを処理していればティックイベントは無視する
            if (CanvasSession.vehicleMoveDuration.isInTime(session.lastVehicleMove)) {
                return
            }
        } else {
            // パケット受信時の場合
            if (player.isInsideVehicle) {
                // 乗り物に乗っている場合かつ移動パケットがある場合 (馬やボートなど)
                if (locationOperation == LocationOperation.POSITION || locationOperation == LocationOperation.POSITION_LOOK) {
                    // 最後の移動時刻を取得
                    session.lastVehicleMove = TimeWatcher.now
                }
            }
        }

        // パケットの座標を合成しプレイヤーの座標と目線を計算
        val playerEyePos = locationOperation.operation(session.eyeLocation, eyeLocation)
        // 目線の座標を更新
        session.eyeLocation = playerEyePos

        // キャンバスが描画中かどうかを確認
        if (!session.tool.isDrawing) {
            return
        }

        // スレッドが違うと問題が起こるためここでclear
        // デバッグ座標を初期化
        player.clearDebug()

        // レイツールを初期化
        val rayTrace = FrameRayTrace(player)
        // レイを飛ばしてアイテムフレームを取得
        val ray = rayTrace.rayTraceCanvas(playerEyePos)
            ?: return

        // 裏からのクリックは無視
        if (!rayTrace.isCanvasFrontSide(playerEyePos.direction, ray.itemFrame)) {
            return
        }

        // キャンバスに描画
        rayTrace.manipulate(
            ray,
            session,
            CanvasActionType.MOUSE_MOVE
        )
    }

    /**
     * パケットのクリックの種類
     */
    private enum class PacketEnumEntityUseAction {
        INTERACT,
        ATTACK,
        INTERACT_AT,
    }

    /**
     * プレイヤーのクリックパケットリスナーを作成する
     * @return プレイヤーのクリックパケットリスナー
     */
    fun createClickPacketAdapter(): PacketAdapter {
        return object : PacketAdapter(
            PaintCraft.instance,
            ListenerPriority.NORMAL,
            listOf(
                PacketType.Play.Client.BLOCK_PLACE,
                PacketType.Play.Client.ARM_ANIMATION,
                PacketType.Play.Client.USE_ITEM,
                PacketType.Play.Client.USE_ENTITY,
            ),
        ) {
            /** 受信 (クライアント→サーバー) */
            override fun onPacketReceiving(event: PacketEvent) {
                // プレイヤーがいなければ無視
                val player = event.player
                    ?: return
                // プレイヤーの右手にインクがあるか
                if (player.hasPencil()) {
                    return
                }

                // パケット解析
                val packet = event.packet
                // クリックの種類を解析
                var targetEntity: Entity? = null
                val clickType = when (event.packetType) {
                    PacketType.Play.Client.BLOCK_PLACE -> CanvasActionType.RIGHT_CLICK
                    PacketType.Play.Client.USE_ITEM -> CanvasActionType.RIGHT_CLICK
                    PacketType.Play.Client.ARM_ANIMATION -> null
                    PacketType.Play.Client.USE_ENTITY -> {
                        // アクションの種類を解析
                        @Suppress("UNCHECKED_CAST")
                        val enumAction =
                            MinecraftReflection.getMinecraftClass("PacketPlayInUseEntity\$EnumEntityUseAction") as Class<Any>
                        val actionType = packet
                            .getEnumModifier(PacketEnumEntityUseAction::class.java, enumAction)
                            .read(0)
                            ?: return
                        // エンティティを取得
                        targetEntity = packet.getEntityModifier(event).read(0)
                        // 右クリックか左クリックか判定
                        when (actionType) {
                            PacketEnumEntityUseAction.INTERACT -> CanvasActionType.RIGHT_CLICK
                            PacketEnumEntityUseAction.INTERACT_AT -> CanvasActionType.RIGHT_CLICK
                            PacketEnumEntityUseAction.ATTACK -> CanvasActionType.LEFT_CLICK
                        }
                    }

                    else -> return
                }

                // setCancelの判断を待ってもらう
                val marker = event.asyncMarker
                marker.incrementProcessingDelay()
                // メインスレッド以外でエンティティを取得できないため、メインスレッドで処理
                Bukkit.getScheduler().runTask(PaintCraft.instance) { ->
                    // パケットを処理し、キャンセルフラグをセット
                    event.isCancelled = onClickPacket(player, clickType, targetEntity)

                    // ロック
                    synchronized(marker.processingLock) {
                        // 待ってもらっていたsetCancelの判断を続行してもらう
                        PaintCraft.instance.protocolManager.asynchronousManager.signalPacketTransmission(event)
                    }
                }
            }
        }
    }

    /**
     * クリックしたとき
     * @param player プレイヤー
     * @param actionType クリックの種類
     * @param targetEntity クリックしたエンティティ (イベントの種類によってはない)
     * @return キャンセルしたかどうか
     */
    private fun onClickPacket(player: Player, actionType: CanvasActionType?, targetEntity: Entity?): Boolean {
        // デバッグ座標を初期化
        player.clearDebug()

        // キャンバスだったら必ず後でキャンセルする (return trueする)
        val isCanvas = targetEntity?.isCanvas() ?: false

        // キャンバスのセッションを取得
        val session = CanvasSessionManager.getSession(player)

        // 目線の位置を取得
        val playerEyePos = session.eyeLocation
        // レイツールを初期化
        val rayTrace = FrameRayTrace(player)
        // レイを飛ばしてアイテムフレームを取得
        val ray = rayTrace.rayTraceCanvas(playerEyePos)
            ?: return isCanvas

        // 裏からのクリックは無視
        if (!rayTrace.isCanvasFrontSide(playerEyePos.direction, ray.itemFrame)) {
            return isCanvas
        }

        // クリックタイプに応じた処理、判定を行う
        val actionTypeRightOrLeft = when (actionType) {
            CanvasActionType.RIGHT_CLICK -> {
                // 最後の右クリック時刻を取得
                session.lastInteract = TimeWatcher.now

                CanvasActionType.RIGHT_CLICK
            }

            null -> {
                // PacketType.Play.Client.ARM_ANIMATIONの場合、エンティティを右クリックすると左クリック判定になることがある
                // 対策として、直前で右クリックが行われていれば右クリックだと判定する
                if (CanvasSession.interactEntityDuration.isInTime(session.lastInteract)) {
                    // エンティティを右クリック後から一定時間経過していなければ右クリック
                    CanvasActionType.RIGHT_CLICK
                } else {
                    // 通常は左クリック
                    CanvasActionType.LEFT_CLICK
                }
            }

            CanvasActionType.LEFT_CLICK -> CanvasActionType.LEFT_CLICK
            else -> return isCanvas
        }

        // キャンバスに描画
        rayTrace.manipulate(
            ray,
            session,
            actionTypeRightOrLeft,
        )

        return true
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