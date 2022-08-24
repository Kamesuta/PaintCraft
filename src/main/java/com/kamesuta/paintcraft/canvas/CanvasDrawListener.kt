package com.kamesuta.paintcraft.canvas

import com.kamesuta.paintcraft.map.MapItem
import com.kamesuta.paintcraft.map.mapSize
import com.kamesuta.paintcraft.util.DebugLocationType
import com.kamesuta.paintcraft.util.DebugLocationVisualizer.clearDebugLocation
import com.kamesuta.paintcraft.util.DebugLocationVisualizer.debugLocation
import com.kamesuta.paintcraft.util.UV
import com.kamesuta.paintcraft.util.UVInt
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
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector
import kotlin.math.abs
import kotlin.math.roundToLong

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
        // UVを計算
        val (rawUV, _) = calculateUV(player.eyeLocation, itemFrame.location, itemFrame.isVisible)
        val uv = transformUV(itemFrame.rotation, rawUV)
            ?: return
        // キャンバスに描画
        manipulate(itemFrame, mapItem, uv, player, CanvasActionType.LEFT_CLICK)
        // イベントをキャンセル
        event.isCancelled = true
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
        // UVを計算
        val (rawUV, _) = calculateUV(player.eyeLocation, itemFrame.location, itemFrame.isVisible)
        val uv = transformUV(itemFrame.rotation, rawUV)
            ?: return
        // キャンバスに描画
        manipulate(itemFrame, mapItem, uv, player, CanvasActionType.RIGHT_CLICK)
        // イベントをキャンセル
        event.isCancelled = true
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

        // レイを飛ばしてアイテムフレームを取得
        val ray = rayTraceCanvas(player, null)
            ?: return

        // キャンバスに描画
        manipulate(
            ray.itemFrame, ray.mapItem, ray.uv, player,
            when (event.action) {
                Action.RIGHT_CLICK_BLOCK -> CanvasActionType.RIGHT_CLICK
                Action.RIGHT_CLICK_AIR -> CanvasActionType.RIGHT_CLICK
                Action.PHYSICAL -> return
                else -> CanvasActionType.LEFT_CLICK
            }
        )
        // イベントをキャンセル
        event.isCancelled = true
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
        val uv: UVInt,
    )

    /**
     * レイを飛ばしてアイテムフレームを取得
     * @param player プレイヤー
     * @param interactionPoint カーソルをのせている場所の座標
     */
    private fun rayTraceCanvas(player: Player, interactionPoint: Location?): CanvasRayTraceResult? {
        // 目線と向きからエンティティを取得し、アイテムフレームかどうかを確認する
        // まず目線の位置と向きを取得
        val playerEyePos = player.eyeLocation
        val playerDirection = playerEyePos.direction
        player.debugLocation(DebugLocationType.EYE_LOCATION, playerEyePos)
        player.debugLocation(DebugLocationType.EYE_DIRECTION, playerEyePos.add(playerDirection))

        // エンティティを取得する範囲のバウンディングボックス
        val box: BoundingBox
        val margin = 1.0
        // クリックがヒットした座標
        val blockHitLocation: Location?
        // クリックしたブロックが取得できるなら使用し、そうでなければレイキャストして取得する
        if (interactionPoint != null) {
            // ブロックとプレイヤーが十分近い場合クリック位置が取得できる
            // クリック位置と目線の座標を含むバウンディングボックスを作成
            box = BoundingBox.of(playerEyePos, interactionPoint)
            blockHitLocation = interactionPoint
        } else {
            // ブロックが取得できなかったら前方8m(半径4)を範囲にする
            val distance = 8.0
            box = BoundingBox.of(playerEyePos, 0.0, 0.0, 0.0).expand(playerDirection, distance)
            // レイキャストを行い、ヒットしたブロックがあればそのブロック座標と目線の位置から範囲の中心座標とサイズを計算する
            val ray =
                playerEyePos.world.rayTraceBlocks(playerEyePos, playerEyePos.direction, distance + margin)
            blockHitLocation = ray?.hitPosition?.toLocation(playerEyePos.world)
        }
        player.debugLocation(DebugLocationType.BLOCK_HIT_LOCATION, blockHitLocation)

        // 範囲内にあるすべてのアイテムフレームを取得する
        val entities = playerEyePos.world.getNearbyEntities(box.clone().expand(margin)) { it is ItemFrame }
            .map { it as ItemFrame }
            // その中からアイテムフレームを取得する
            .filter { it.item.type == Material.FILLED_MAP }
            // 正面に向いているアイテムフレームのみを取得する
            .filter { playerDirection.dot(Vector(it.facing.modX, it.facing.modY, it.facing.modZ)) <= 0 }
            // 最も近いエンティティを取得するために距離順にソートする
            .sortedBy { it.location.distance(playerEyePos) }

        // 一番近いヒットしたキャンバスに描画する
        for (itemFrame in entities) {
            // マップデータを取得、ただの地図ならばスキップ
            val mapItem = MapItem.get(itemFrame.item)
                ?: continue
            // UVとキャンバスのオフセットを計算
            val (rawUV, canvasOffset) = calculateUV(player.eyeLocation, itemFrame.location, itemFrame.isVisible)
            // キャンバス内UVを計算、キャンバス範囲外ならばスキップ
            val uv = transformUV(itemFrame.rotation, rawUV)
                ?: continue
            // キャンバスよりも手前にブロックがあるならばスキップ
            if (blockHitLocation != null) {
                val blockDistance = blockHitLocation.distance(playerEyePos)
                val canvasHitLocation = itemFrame.location.clone().add(canvasOffset)
                player.debugLocation(DebugLocationType.CANVAS_HIT_LOCATION, canvasHitLocation)
                val canvasDistance = canvasHitLocation.distance(playerEyePos)
                if (blockDistance + 0.5 < canvasDistance) {
                    continue
                }
            }
            return CanvasRayTraceResult(itemFrame, mapItem, uv)
        }
        return null;
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
        uv: UVInt,
        player: Player,
        actionType: CanvasActionType
    ) {
        // アイテムフレームの位置を取得
        val itemFrameLocation = itemFrame.location
        // アイテムフレームが貼り付いているブロックの位置を計算する
        val blockLocation = itemFrameLocation.clone().add(
            -0.5 * itemFrame.facing.modX,
            -0.5 * itemFrame.facing.modY,
            -0.5 * itemFrame.facing.modZ,
        )
        // インタラクトオブジェクトを作成
        val interact = CanvasInteraction(uv, player, blockLocation, itemFrameLocation, actionType)

        // キャンバスのセッションを取得
        val session = CanvasSessionManager.getSession(player)
        // キャンバスに描画する
        session.tool.paint(player.inventory.itemInMainHand, mapItem, interact, session)
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
     * プレイヤーの視点からキャンバス内のブロックUV座標を計算する
     * @param playerEyePos プレイヤーの目線位置
     * @param itemFrameLocation アイテムフレームの座標
     * @param isFrameVisible アイテムフレームが見えるかどうか
     * @return UV座標とキャンバスのオフセット
     */
    private fun calculateUV(
        playerEyePos: Location,
        itemFrameLocation: Location,
        isFrameVisible: Boolean
    ): Pair<UV, Vector> {
        // プレイヤーの目線の方向
        val playerDirection = playerEyePos.direction
        // アイテムフレームの正面ベクトル
        val itemFrameDirection = itemFrameLocation.direction

        // アイテムフレームの正面の整数ベクトル
        val frameDirection = itemFrameDirection.let {
            val x = it.x.roundToLong().toDouble()
            val y = it.y.roundToLong().toDouble()
            val z = it.z.roundToLong().toDouble()
            Vector(x, y, z)
        }

        // アイテムフレームから目線へのベクトル
        val itemFrameToEye = playerEyePos.toVector().subtract(itemFrameLocation.toVector())

        // 目線上のキャンバス座標のオフセットを計算
        val v1 = frameDirection.clone().dot(itemFrameToEye)
        val v0 = frameDirection.clone().dot(playerDirection)
        val miu = v1 / v0 + if (isFrameVisible) 0.04 else -0.04
        val lookOffset = itemFrameToEye.clone().subtract(playerDirection.clone().multiply(miu))

        // 各向きについてUV座標を計算する
        val u = if (abs(frameDirection.x) > abs(frameDirection.z)) {
            if (frameDirection.x > 0)
                -lookOffset.z // 西向き
            else
                lookOffset.z // 東向き
        } else {
            if (abs(frameDirection.y) > 0)
                lookOffset.x // 上向き, 下向き
            else if (frameDirection.z > 0)
                lookOffset.x // 北向き
            else
                -lookOffset.x // 南向き
        }
        val v = if (abs(frameDirection.y) > 0) {
            if (frameDirection.y > 0)
                lookOffset.z // 上向き
            else
                -lookOffset.z // 下向き
        } else {
            -lookOffset.y // 横向き
        }
        return UV(u, v) to lookOffset
    }
}