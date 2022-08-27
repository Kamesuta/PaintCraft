package com.kamesuta.paintcraft.canvas

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.kamesuta.paintcraft.PaintCraft
import com.kamesuta.paintcraft.map.MapItem
import com.kamesuta.paintcraft.map.mapSize
import com.kamesuta.paintcraft.util.DebugLocationType
import com.kamesuta.paintcraft.util.DebugLocationVisualizer.clearDebugLocation
import com.kamesuta.paintcraft.util.DebugLocationVisualizer.debugLocation
import com.kamesuta.paintcraft.util.LocationOperation
import com.kamesuta.paintcraft.util.UV
import com.kamesuta.paintcraft.util.UVInt
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Rotation
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector
import kotlin.math.asin
import kotlin.math.atan2

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
        player.clearDebugLocation(DebugLocationType.DebugLocationGroup.CANVAS_DRAW)
        // プレイヤーの右手にインクがあるか
        if (player.inventory.itemInMainHand.type != Material.INK_SAC) {
            return
        }

        // アイテムフレームを取得
        val itemFrame = event.entity as? ItemFrame
            ?: return
        // アイテムが地図かどうかを確認
        if (itemFrame.item.type != Material.FILLED_MAP) {
            return
        }
        // キャンバスか判定し取得
        val mapItem = MapItem.get(itemFrame.item)
            ?: return
        // イベントをキャンセル (ここに来た時点でキャンバスを左クリックしているのでアイテムフレームが破壊されないようにキャンセルする)
        event.isCancelled = true

        // キャンバスのセッションを取得
        val session = CanvasSessionManager.getSession(player)

        // レイを飛ばしてチェックしアイテムフレームを取得
        // (イベントからエンティティが取れるが、前後関係でより近い額縁がある事があるのでレイを飛ばしてエンティティを取得する)
        val ray = rayTraceCanvas(session.eyeLocation, player)
            ?: return

        // 裏からのクリックは無視
        if (!isCanvasFrontSide(session.eyeLocation.direction, ray.itemFrame)) {
            return
        }

        // キャンバスに描画
        manipulate(ray.itemFrame, ray.mapItem, ray.canvasOffset, ray.uv, player, session, CanvasActionType.LEFT_CLICK)
    }

    /**
     * アイテムフレームを直接右クリックしたときに描画する
     * @param event イベント
     */
    @EventHandler
    fun onInteractEntity(event: PlayerInteractEntityEvent) {
        // 右クリックしたプレイヤー
        val player = event.player
        player.clearDebugLocation(DebugLocationType.DebugLocationGroup.CANVAS_DRAW)
        // プレイヤーの右手にインクがあるか
        if (player.inventory.itemInMainHand.type != Material.INK_SAC) {
            return
        }

        // アイテムフレームを取得
        val itemFrame = event.rightClicked as? ItemFrame
            ?: return
        // アイテムが地図かどうかを確認
        if (itemFrame.item.type != Material.FILLED_MAP) {
            return
        }
        // キャンバスか判定し取得
        val mapItem = MapItem.get(itemFrame.item)
            ?: return
        // イベントをキャンセル (ここに来た時点でキャンバスを右クリックしているのでアイテムフレームが回転しないようにキャンセルする)
        event.isCancelled = true

        // キャンバスのセッションを取得
        val session = CanvasSessionManager.getSession(player)

        // レイを飛ばしてチェックしアイテムフレームを取得
        // (イベントからエンティティが取れるが、前後関係でより近い額縁がある事があるのでレイを飛ばしてエンティティを取得する)
        val ray = rayTraceCanvas(session.eyeLocation, player)
            ?: return

        // 裏からのクリックは無視
        if (!isCanvasFrontSide(session.eyeLocation.direction, ray.itemFrame)) {
            return
        }

        // キャンバスに描画
        manipulate(ray.itemFrame, ray.mapItem, ray.canvasOffset, ray.uv, player, session, CanvasActionType.RIGHT_CLICK)
    }

    /**
     * アイテムフレームを遠くから左右クリックしたときに描画する
     * @param event イベント
     */
    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        // 左右クリックしたプレイヤー
        val player = event.player
        player.clearDebugLocation(DebugLocationType.DebugLocationGroup.CANVAS_DRAW)
        // プレイヤーの右手にインクがあるか
        if (player.inventory.itemInMainHand.type != Material.INK_SAC) {
            return
        }

        // キャンバスのセッションを取得
        val session = CanvasSessionManager.getSession(player)

        // レイを飛ばしてアイテムフレームを取得
        val ray = rayTraceCanvas(session.eyeLocation, player)
            ?: return

        // イベントをキャンセル
        event.isCancelled = true

        // 裏からのクリックは無視
        if (!isCanvasFrontSide(session.eyeLocation.direction, ray.itemFrame)) {
            return
        }

        // キャンバスに描画
        manipulate(
            ray.itemFrame, ray.mapItem, ray.canvasOffset, ray.uv, player, session,
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
        if (player.inventory.itemInMainHand.type != Material.INK_SAC) {
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
            player.clearDebugLocation(DebugLocationType.DebugLocationGroup.CANVAS_DRAW)

            // レイを飛ばしてアイテムフレームを取得
            val ray = rayTraceCanvas(playerEyePos, player)
                ?: return@runTask

            // 裏からのクリックは無視
            if (!isCanvasFrontSide(playerEyePos.direction, ray.itemFrame)) {
                return@runTask
            }

            // キャンバスに描画
            manipulate(
                ray.itemFrame,
                ray.mapItem,
                ray.canvasOffset,
                ray.uv,
                player,
                session,
                CanvasActionType.MOUSE_MOVE
            )
        }
    }

    /**
     * キャンバス上のヒットした位置情報
     * @param itemFrame アイテムフレーム
     * @param mapItem 地図アイテム
     * @param uv UV
     */
    private data class CanvasRayTraceResult(
        val itemFrame: ItemFrame,
        val mapItem: MapItem,
        val canvasOffset: Vector,
        val uv: UVInt,
    )

    /**
     * 指定されたアイテムフレームにレイを飛ばして一致する場合は取得
     * @param playerEyePos プレイヤーの目線の位置
     * @param debugPlayer プレイヤー
     * @param itemFrame アイテムフレーム
     */
    private fun rayTraceCanvasByEntity(
        playerEyePos: Location,
        debugPlayer: Player,
        itemFrame: ItemFrame,
    ): CanvasRayTraceResult? {
        // マップデータを取得、ただの地図ならばスキップ
        val mapItem = MapItem.get(itemFrame.item)
            ?: return null
        // キャンバスのオフセットを計算
        val itemFrameLocation = itemFrame.location
        val canvasOffset = intersectCanvas(playerEyePos, itemFrameLocation, itemFrame.isVisible, debugPlayer)
        // UVに変換
        val rawUV = mapToBlockUV(itemFrameLocation.yaw, itemFrameLocation.pitch, canvasOffset)
        // キャンバス内UVを計算、キャンバス範囲外ならばスキップ
        val uv = transformUV(itemFrame.rotation, rawUV)
            ?: return null
        return CanvasRayTraceResult(itemFrame, mapItem, canvasOffset, uv)
    }

    /**
     * キャンバスが表か判定する
     * @param playerDirection プレイヤーの方向
     * @param itemFrame アイテムフレーム
     * @return キャンバスが表かどうか
     */
    private fun isCanvasFrontSide(playerDirection: Vector, itemFrame: ItemFrame): Boolean {
        // 裏からのクリックを判定
        return playerDirection.dot(itemFrame.location.toCanvasLocation().direction) <= 0
    }

    /**
     * レイを飛ばしてアイテムフレームを取得
     * @param playerEyePos プレイヤーの目線の位置
     * @param debugPlayer プレイヤー
     */
    private fun rayTraceCanvas(
        playerEyePos: Location,
        debugPlayer: Player
    ): CanvasRayTraceResult? {
        // 目線と向きからエンティティを取得し、アイテムフレームかどうかを確認する
        // まず目線の位置と向きを取得
        val playerDirection = playerEyePos.direction
        debugPlayer.debugLocation(DebugLocationType.EYE_LOCATION, playerEyePos)
        debugPlayer.debugLocation(DebugLocationType.EYE_DIRECTION, playerEyePos.clone().add(playerDirection))

        // 距離は前方8m(半径4)を範囲にする
        val distance = 8.0
        // 範囲を全方向にmarginずつ拡張
        val margin = 1.0
        // エンティティを取得する範囲のバウンディングボックス
        val box = BoundingBox.of(playerEyePos, 0.0, 0.0, 0.0).expand(playerDirection, distance)
        // レイキャストを行い、ヒットしたブロックがあればそのブロック座標と目線の位置から範囲の中心座標とサイズを計算する
        val ray = playerEyePos.world.rayTraceBlocks(playerEyePos, playerEyePos.direction, distance + margin)
        // クリックがヒットした座標
        val blockHitLocation = ray?.hitPosition?.toLocation(playerEyePos.world)
        debugPlayer.debugLocation(DebugLocationType.BLOCK_HIT_LOCATION, blockHitLocation)

        // 範囲内にあるすべてのアイテムフレームを取得する
        val entities = playerEyePos.world.getNearbyEntities(box.clone().expand(margin)) { it is ItemFrame }
            .map { it as ItemFrame }
            // その中からアイテムフレームを取得する
            .filter { it.item.type == Material.FILLED_MAP }
            // 最も近いエンティティを取得するために距離順にソートする
            .sortedBy {
                // キャンバス平面の位置 (tpでアイテムフレームを回転したときにずれる)
                val canvasLocation = it.location.toCanvasLocation()
                // 距離の2条で比較する
                canvasLocation.distanceSquared(playerEyePos)
            }

        // 一番近いヒットしたキャンバスに描画する
        for (itemFrame in entities) {
            // マップデータを取得、ただの地図ならばスキップ
            val mapItem = MapItem.get(itemFrame.item)
                ?: continue
            // キャンバスのオフセットを計算
            val itemFrameLocation = itemFrame.location
            val canvasOffset = intersectCanvas(playerEyePos, itemFrameLocation, itemFrame.isVisible, debugPlayer)
            // UVに変換
            val rawUV = mapToBlockUV(itemFrameLocation.yaw, itemFrameLocation.pitch, canvasOffset)
            // キャンバス内UVを計算、キャンバス範囲外ならばスキップ
            val uv = transformUV(itemFrame.rotation, rawUV)
                ?: continue
            // キャンバスよりも手前にブロックがあるならば探索終了
            if (blockHitLocation != null) {
                val blockDistance = blockHitLocation.distance(playerEyePos)
                val canvasHitLocation = itemFrameLocation.clone().add(canvasOffset)
                val canvasDistance = canvasHitLocation.distance(playerEyePos)
                if (blockDistance + 0.5 < canvasDistance) {
                    break
                }
            }
            return CanvasRayTraceResult(itemFrame, mapItem, canvasOffset, uv)
        }
        return null
    }

    /**
     * キャンバスに描画する
     * @param itemFrame アイテムフレーム
     * @param mapItem マップデータ
     * @param uv UV座標
     * @param player プレイヤー
     * @param actionType アクションタイプ
     */
    private fun manipulate(
        itemFrame: ItemFrame,
        mapItem: MapItem,
        canvasOffset: Vector,
        uv: UVInt,
        player: Player,
        session: CanvasSession,
        actionType: CanvasActionType
    ) {
        // アイテムフレームの位置を取得
        val itemFrameLocation = itemFrame.location
        player.debugLocation(DebugLocationType.FRAME_LOCATION, itemFrameLocation)
        player.debugLocation(
            DebugLocationType.FRAME_DIRECTION,
            itemFrameLocation.clone().add(itemFrameLocation.direction)
        )
        player.debugLocation(DebugLocationType.FRAME_FACING, itemFrameLocation.clone().add(itemFrame.facing.direction))
        player.debugLocation(DebugLocationType.FRAME_FACING_BLOCK, itemFrameLocation.toCenterLocation())

        // アイテムフレームの位置を取得
        val canvasLocation = itemFrameLocation.toCanvasLocation()
        player.debugLocation(DebugLocationType.CANVAS_LOCATION, canvasLocation)
        // アイテムフレームの正面ベクトル
        player.debugLocation(
            DebugLocationType.CANVAS_DIRECTION,
            canvasLocation.clone().add(canvasLocation.direction)
        )

        // ヒット位置
        val canvasHitLocation = canvasLocation.clone().add(canvasOffset)
        player.debugLocation(DebugLocationType.CANVAS_HIT_LOCATION, canvasHitLocation)

        // アイテムフレームが貼り付いているブロックの位置を計算する
        val blockLocation = canvasLocation.clone().add(
            -0.5 * itemFrame.facing.modX,
            -0.5 * itemFrame.facing.modY,
            -0.5 * itemFrame.facing.modZ,
        )
        // インタラクトオブジェクトを作成
        val interact = CanvasInteraction(uv, player, blockLocation, canvasLocation, actionType)

        // キャンバスに描画する
        session.tool.paint(player.inventory.itemInMainHand, mapItem, interact)
        // プレイヤーに描画を通知する
        mapItem.renderer.updatePlayer(player)
    }

    /**
     * ブロックのUV座標->キャンバスピクセルのUV座標を計算する
     * @param rotation アイテムフレーム内の地図の回転
     * @param uv ブロックのUV座標
     * @return キャンバスピクセルのUV座標
     */
    private fun transformUV(rotation: Rotation, uv: UV): UVInt? {
        // BukkitのRotationからCanvasのRotationに変換する
        val rot: CanvasRotation = CanvasRotation.fromRotation(rotation)
        // -0.5～0.5の範囲を0.0～1.0の範囲に変換する
        val q = UV(rot.u(uv) + 0.5, rot.v(uv) + 0.5)
        // 0～128(ピクセル座標)の範囲に変換する
        val x = (q.u * mapSize).toInt()
        val y = (q.v * mapSize).toInt()
        // 範囲外ならばnullを返す
        if (x >= mapSize || x < 0) return null
        if (y >= mapSize || y < 0) return null
        // 変換した座標を返す
        return UVInt(x, y)
    }

    /**
     * プレイヤーの視点とアイテムフレームの位置から交点の座標を計算する
     * @param playerEyePos プレイヤーの目線位置
     * @param itemFrameLocation アイテムフレームの座標
     * @param isFrameVisible アイテムフレームが見えるかどうか
     * @return 交点座標
     */
    private fun intersectCanvas(
        playerEyePos: Location,
        itemFrameLocation: Location,
        isFrameVisible: Boolean,
        debugPlayer: Player,
    ): Vector {
        // プレイヤーの目線の方向
        val playerDirection = playerEyePos.direction

        // キャンバス平面の位置 (tpでアイテムフレームを回転したときにずれる)
        val canvasLocation = itemFrameLocation.toCanvasLocation()
        // アイテムフレームの正面ベクトル
        val canvasDirection = canvasLocation.direction

        // キャンバス平面とアイテムフレームの差 = アイテムフレームの厚さ/2
        val canvasOffsetZ = if (isFrameVisible) 0.07 else 0.0075
        // キャンバスの表面の平面の座標 = アイテムフレームエンティティの中心からアイテムフレームの厚さ/2だけずらした位置
        val canvasPlane = canvasLocation.clone().add(canvasDirection.clone().multiply(canvasOffsetZ))

        // アイテムフレームから目線へのベクトル
        val canvasPlaneToEye = playerEyePos.toVector().subtract(canvasPlane.toVector())

        // 目線上のキャンバス座標のオフセットを計算 (平面とベクトルとの交点)
        // https://qiita.com/edo_m18/items/c8808f318f5abfa8af1e
        // http://www.sousakuba.com/Programming/gs_plane_line_intersect.html
        val v1 = canvasDirection.clone().dot(canvasPlaneToEye)
        val v0 = canvasDirection.clone().dot(playerDirection)

        // 交点の座標を求める
        return canvasPlaneToEye.clone().subtract(playerDirection.clone().multiply(v1 / v0))
    }

    /**
     * 交点座標をキャンバス上のUV座標に変換する
     * UV座標は中央が(0,0)になる
     * @param itemFrameYaw アイテムフレームのYaw角度
     * @param itemFramePitch アイテムフレームのPitch角度
     * @param intersectPosition 交点座標
     * @return キャンバス上のUV座標
     */
    private fun mapToBlockUV(
        itemFrameYaw: Float,
        itemFramePitch: Float,
        intersectPosition: Vector
    ): UV {
        // 交点座標を(0,0)を中心に回転し、UV座標(x,-y)に対応するようにする
        val unRotated = intersectPosition.clone()
            .rotateAroundX(Math.toRadians(-itemFramePitch.toDouble()))
            .rotateAroundY(Math.toRadians(itemFrameYaw.toDouble()))
        // UV座標を返す (3D座標はYが上ほど大きく、UV座標はYが下ほど大きいため、Yを反転する)
        return UV(unRotated.x, -unRotated.y)
    }

    /**
     * キャンバスフレームの平面の座標を求める
     * アイテムフレームの座標からキャンバス平面の座標を計算する
     * @return キャンバスフレームの平面の座標
     */
    private fun Location.toCanvasLocation(): Location {
        // キャンバスの向き。通常のdirectionとはpitchが反転していることに注意
        // Y軸回転→X軸回転をX軸回転→Y軸回転にするために、一旦単位方向ベクトルに変換
        val dir = Vector(0.0, 0.0, 1.0)
            .rotateAroundY(Math.toRadians(-yaw.toDouble()))
            .rotateAroundX(Math.toRadians(pitch.toDouble()))

        // 方向ベクトルからyawとpitchを求める
        val center = toCenterLocation()
        center.yaw = Math.toDegrees(-atan2(dir.x, dir.z)).toFloat()
        center.pitch = Math.toDegrees(asin(-dir.y)).toFloat()

        // 中心の座標ををキャンバスの向き方向にずらす
        return center.subtract(dir.multiply(0.5))
    }
}