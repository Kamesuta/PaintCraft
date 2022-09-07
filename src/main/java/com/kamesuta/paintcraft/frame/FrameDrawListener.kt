package com.kamesuta.paintcraft.frame

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.utility.MinecraftReflection
import com.kamesuta.paintcraft.PaintCraft
import com.kamesuta.paintcraft.canvas.*
import com.kamesuta.paintcraft.canvas.paint.PaintEvent
import com.kamesuta.paintcraft.util.LocationOperation
import com.kamesuta.paintcraft.util.TimeWatcher
import com.kamesuta.paintcraft.util.vec.Line3d.Companion.toLine
import com.kamesuta.paintcraft.util.vec.debug.DebugLocationType
import com.kamesuta.paintcraft.util.vec.debug.DebugLocationVisualizer.clearDebugLocation
import com.kamesuta.paintcraft.util.vec.debug.DebugLocationVisualizer.debugLocation
import com.kamesuta.paintcraft.util.vec.origin
import com.kamesuta.paintcraft.util.vec.plus
import com.kamesuta.paintcraft.util.vec.target
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import java.util.logging.Level

/**
 * 描画用のイベントリスナー
 */
class FrameDrawListener : Listener, Runnable {
    /**
     * ティックイベント
     */
    override fun run() {
        // トロッコなど乗っていた場合などの処理のためにティックイベントも動かす
        for (player in Bukkit.getOnlinePlayers()) {
            // プレイヤーの右手にインクがあるか
            if (!player.hasPencil()) {
                continue
            }

            try {
                // パケットを処理
                onMovePacket(player, player.eyeLocation, LocationOperation.POSITION, true)
            } catch (e: Throwable) {
                // スケジューラーに例外を投げないためにキャッチする
                PaintCraft.instance.logger.log(
                    Level.WARNING,
                    "Error occurred while tick event for player " + player.name,
                    e
                )
            }
        }
    }

    /**
     * プレイヤーがインクを持っていたらイベントをキャンセルする
     * パケットをキャンセルすることも可能だが、そうするとブロックを壊したときに同期がずれて透明な当たり判定が残ってしまうためBukkitの方法でキャンセルする
     * @param event イベント
     */
    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        // 物理イベントは無視 (感圧板など)
        if (event.action == Action.PHYSICAL) {
            return
        }

        // プレイヤーの右手にインクがあるか
        if (!event.player.hasPencil()) {
            return
        }

        // インタラクトをキャンセル
        event.isCancelled = true
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

                try {
                    // プレイヤーの右手にインクがあるか
                    if (!player.hasPencil()) {
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
                        try {
                            // パケットを処理
                            onMovePacket(player, location, locationOperation, false)
                        } catch (e: Throwable) {
                            // スケジューラーに例外を投げないためにキャッチする
                            PaintCraft.instance.logger.log(
                                Level.WARNING,
                                "Error occurred while move packet event for player " + player.name,
                                e
                            )
                        }
                    }
                } catch (e: Throwable) {
                    // パケット処理内で例外をthrowすると同期ズレの原因になるため、エラーメッセージを出力して処理しておく
                    PaintCraft.instance.logger.log(
                        Level.WARNING,
                        "Failed to process move packet for player " + player.name,
                        e
                    )
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
            if (session.clientType.threshold.vehicleMoveDuration.isInTime(session.lastVehicleMove)) {
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

        // パケットの座標を合成しプレイヤーの座標と目線を計算し、目線の座標を更新
        session.eyeLocation = locationOperation.operation(session.eyeLocation, eyeLocation)

        // スレッドが違うと問題が起こるためここでclear
        // デバッグ座標を初期化
        player.clearDebug()

        // クリック状態の更新
        session.clicking.updateClick(CanvasActionType.MOUSE_MOVE)

        // クリック中かどうかを確認
        if (!session.clicking.clickMode.isPressed) {
            // クリック状態の変化を確認
            val drawingAction = session.drawing.getDrawingAction(session.clicking.clickMode.isPressed)
            if (drawingAction == CanvasDrawingActionType.END) {
                // クリック中でない場合、描画終了時の処理
                session.drawing.endDrawing()
                session.tool.endPainting()
            }
            // クリック中でない場合は描き込みを行わない
            return
        }

        // レイツールを初期化
        val rayTrace = FrameRayTrace(player, session.clientType)
        // レイを飛ばしてアイテムフレームを取得
        val eyeLocation = session.eyeLocation.toLine()
        val ray = rayTrace.rayTraceCanvas(eyeLocation)
            ?: return

        // 裏からのクリックは無視
        if (!rayTrace.isCanvasFrontSide(eyeLocation.direction, ray.canvasLocation)) {
            return
        }

        // キャンバスに描画
        manipulate(
            ray,
            session,
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

                try {
                    // プレイヤーの右手にインクがあるか
                    if (!player.hasPencil()) {
                        return
                    }

                    // アイテムを持っていたらクリックはキャンセル
                    event.isCancelled = true

                    // パケット解析
                    val packet = event.packet
                    // クリックの種類を解析
                    val actionTypeRightOrLeft = when (event.packetType) {
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
                            // 右クリックか左クリックか判定
                            when (actionType) {
                                PacketEnumEntityUseAction.INTERACT -> CanvasActionType.RIGHT_CLICK
                                PacketEnumEntityUseAction.INTERACT_AT -> CanvasActionType.RIGHT_CLICK
                                PacketEnumEntityUseAction.ATTACK -> CanvasActionType.LEFT_CLICK
                            }
                        }

                        else -> return
                    }

                    // メインスレッド以外でエンティティを取得できないため、メインスレッドで処理
                    Bukkit.getScheduler().runTask(PaintCraft.instance) { ->
                        try {
                            // パケットを処理
                            onClickPacket(player, actionTypeRightOrLeft)
                        } catch (e: Throwable) {
                            // スケジューラーに例外を投げないためにキャッチする
                            PaintCraft.instance.logger.log(
                                Level.WARNING,
                                "Error occurred while click packet event for player " + player.name,
                                e
                            )
                        }
                    }
                } catch (e: Throwable) {
                    // パケット処理内で例外をthrowすると同期ズレの原因になるため、エラーメッセージを出力して処理しておく
                    PaintCraft.instance.logger.log(
                        Level.WARNING,
                        "Failed to process click packet for player " + player.name,
                        e
                    )
                }
            }
        }
    }

    /**
     * クリックしたとき
     * @param player プレイヤー
     * @param actionType クリックの種類
     * @return キャンセルしたかどうか
     */
    private fun onClickPacket(player: Player, actionType: CanvasActionType?) {
        // デバッグ座標を初期化
        player.clearDebug()

        // キャンバスのセッションを取得
        val session = CanvasSessionManager.getSession(player)

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
                if (session.clientType.threshold.interactEntityDuration.isInTime(session.lastInteract)) {
                    // エンティティを右クリック後から一定時間経過していなければ右クリック
                    CanvasActionType.RIGHT_CLICK
                } else {
                    // 通常は左クリック
                    CanvasActionType.LEFT_CLICK
                }
            }

            CanvasActionType.LEFT_CLICK -> CanvasActionType.LEFT_CLICK
            else -> return
        }

        // クリック状態を更新
        session.clicking.updateClick(actionTypeRightOrLeft)

        // 目線の位置を取得
        val eyeLocation = session.eyeLocation.toLine()
        // レイツールを初期化
        val rayTrace = FrameRayTrace(player, session.clientType)
        // レイを飛ばしてアイテムフレームを取得
        val ray = rayTrace.rayTraceCanvas(eyeLocation)
            ?: return

        // 裏からのクリックは無視
        if (!rayTrace.isCanvasFrontSide(eyeLocation.direction, ray.canvasLocation)) {
            return
        }

        // キャンバスに描画
        manipulate(
            ray,
            session,
        )
    }

    /**
     * キャンバスに描画する
     * @param ray レイ
     * @param session セッション
     */
    private fun manipulate(
        ray: FrameRayTraceResult,
        session: CanvasSession,
    ) {
        // プレイヤーを取得
        val player = session.player

        // アイテムフレームの位置を取得
        val itemFrameLocation = ray.itemFrame.location
        player.debugLocation {
            // アイテムフレームの位置
            locate(DebugLocationType.FRAME_LOCATION, itemFrameLocation.origin)
            // アイテムフレームの方向
            locate(
                DebugLocationType.FRAME_DIRECTION,
                itemFrameLocation.target
            )
            // アイテムフレームのブロック上での方向
            locate(
                DebugLocationType.FRAME_FACING,
                itemFrameLocation.origin + ray.itemFrame.facing.direction
            )
            // アイテムフレームのブロック
            locate(DebugLocationType.FRAME_FACING_BLOCK, itemFrameLocation.toCenterLocation().origin)
            // ヒット位置
            locate(DebugLocationType.CANVAS_HIT_LOCATION, ray.canvasIntersectLocation)
        }

        // インタラクトオブジェクトを作成
        val interact = CanvasInteraction(ray.uv, ray, player)
        val paintEvent = PaintEvent(ray.mapItem, interact, session.clicking.clickMode)

        // クリック状態の変化を確認
        if (!session.drawing.isDrawing) {
            // 描きこみ開始
            session.drawing.beginDrawing(paintEvent)
            session.tool.beginPainting(paintEvent)
        }

        // キャンバスに描画する
        session.tool.paint(paintEvent)
    }

    companion object {
        /**
         * デバッグ座標を初期化
         */
        private fun Player.clearDebug() = clearDebugLocation(DebugLocationType.DebugLocationGroup.CANVAS_DRAW)

        /**
         * プレイヤーがペンを持っているかどうかを確認する
         * @return ペンを持っているかどうか
         */
        private fun Player.hasPencil() =
            gameMode != GameMode.SPECTATOR && inventory.itemInMainHand.type == Material.INK_SAC
    }
}