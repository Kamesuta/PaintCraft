package com.kamesuta.paintcraft.canvas

import com.kamesuta.paintcraft.map.MapItem
import com.kamesuta.paintcraft.map.mapSize
import com.kamesuta.paintcraft.util.DebugLocationType
import com.kamesuta.paintcraft.util.DebugLocationVisualizer.debugLocation
import com.kamesuta.paintcraft.util.UV
import com.kamesuta.paintcraft.util.UVInt
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Rotation
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector
import kotlin.math.asin
import kotlin.math.atan2

/**
 * キャンバスと目線の交差判定をし、UVを計算します
 * @param player プレイヤー
 */
class CanvasRayTrace(private val player: Player) {
    /**
     * キャンバス上のヒットした位置情報
     * @param itemFrame アイテムフレーム
     * @param mapItem 地図アイテム
     * @param canvasLocation キャンバス上の位置
     * @param canvasIntersectOffset キャンバス上の位置とアイテムフレーム上の位置の差分
     * @param uv UV
     */
    data class CanvasRayTraceResult(
        val itemFrame: ItemFrame,
        val mapItem: MapItem,
        val canvasLocation: Location,
        val canvasIntersectOffset: Vector,
        val uv: UVInt,
    ) {
        val canvasIntersectLocation: Location by lazy { canvasLocation.clone().add(canvasIntersectOffset) }
    }

    /**
     * キャンバスが表か判定する
     * @param playerDirection プレイヤーの方向
     * @param itemFrame アイテムフレーム
     * @return キャンバスが表かどうか
     */
    fun isCanvasFrontSide(playerDirection: Vector, itemFrame: ItemFrame): Boolean {
        // 裏からのクリックを判定
        return playerDirection.dot(itemFrame.location.toCanvasLocation().direction) <= 0
    }

    /**
     * レイを飛ばしてアイテムフレームを取得
     * @param playerEyePos プレイヤーの目線の位置
     */
    fun rayTraceCanvas(
        playerEyePos: Location,
    ): CanvasRayTraceResult? {
        // 目線と向きからエンティティを取得し、アイテムフレームかどうかを確認する
        // まず目線の位置と向きを取得
        val playerDirection = playerEyePos.direction
        player.debugLocation(DebugLocationType.EYE_LOCATION, playerEyePos)
        player.debugLocation(DebugLocationType.EYE_DIRECTION, playerEyePos.clone().add(playerDirection))

        // 距離は前方8m(半径4)を範囲にする
        val distance = 8.0
        // 範囲を全方向にmarginずつ拡張
        val margin = 1.0
        // エンティティを取得する範囲のバウンディングボックス
        val box = BoundingBox.of(playerEyePos, 0.0, 0.0, 0.0).expand(playerDirection, distance)
        // レイキャストを行い、ヒットしたブロックがあればそのブロック座標と目線の位置から範囲の中心座標とサイズを計算する
        val blockRay = playerEyePos.world.rayTraceBlocks(playerEyePos, playerEyePos.direction, distance + margin)
        // クリックがヒットした座標
        val blockHitLocation = blockRay?.hitPosition?.toLocation(playerEyePos.world)
        player.debugLocation(DebugLocationType.BLOCK_HIT_LOCATION, blockHitLocation)

        // キャンバスよりも手前にブロックがあるならば探索終了
        val maxDistance = (blockHitLocation?.distance(playerEyePos) ?: distance)

        // 範囲内にあるすべてのアイテムフレームを取得する
        val result = playerEyePos.world.getNearbyEntities(box.clone().expand(margin)) { it is ItemFrame }
            .asSequence()
            .map { it as ItemFrame }
            // その中からアイテムフレームを取得する
            .filter { it.item.type == Material.FILLED_MAP }
            // レイを飛ばす
            .mapNotNull { rayTraceCanvasByEntity(playerEyePos, it) }
            .filter { it.canvasIntersectLocation.distanceSquared(playerEyePos) <= maxDistance * maxDistance }
            // 一番近いヒットしたキャンバス
            .minByOrNull {
                // 距離の2条で比較する
                it.canvasIntersectLocation.distanceSquared(playerEyePos)
            }
            ?: return null

        // 最大距離より遠い場合は除外 (ブロックより後ろのアイテムフレームは除外)
        if (result.canvasIntersectLocation.distanceSquared(playerEyePos) > maxDistance * maxDistance) {
            return null
        }

        return result
    }

    /**
     * キャンバスに描画する
     * @param ray レイ
     * @param session セッション
     * @param actionType アクションタイプ
     */
    fun manipulate(
        ray: CanvasRayTraceResult,
        session: CanvasSession,
        actionType: CanvasActionType
    ) {
        // アイテムフレームの位置を取得
        val itemFrameLocation = ray.itemFrame.location
        player.debugLocation(DebugLocationType.FRAME_LOCATION, itemFrameLocation)
        player.debugLocation(
            DebugLocationType.FRAME_DIRECTION,
            itemFrameLocation.clone().add(itemFrameLocation.direction)
        )
        player.debugLocation(
            DebugLocationType.FRAME_FACING,
            itemFrameLocation.clone().add(ray.itemFrame.facing.direction)
        )
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
        player.debugLocation(DebugLocationType.CANVAS_HIT_LOCATION, ray.canvasIntersectLocation)

        // アイテムフレームが貼り付いているブロックの位置を計算する
        val blockLocation = canvasLocation.clone().add(
            -0.5 * ray.itemFrame.facing.modX,
            -0.5 * ray.itemFrame.facing.modY,
            -0.5 * ray.itemFrame.facing.modZ,
        )
        // インタラクトオブジェクトを作成
        val interact = CanvasInteraction(ray.uv, player, blockLocation, canvasLocation, actionType)

        // キャンバスに描画する
        session.tool.paint(player.inventory.itemInMainHand, ray.mapItem, interact)
        // プレイヤーに描画を通知する
        ray.mapItem.renderer.updatePlayer(player)
    }

    /**
     * 指定されたアイテムフレームにレイを飛ばして一致する場合は取得
     * @param playerEyePos プレイヤーの目線の位置
     * @param itemFrame アイテムフレーム
     */
    private fun rayTraceCanvasByEntity(
        playerEyePos: Location,
        itemFrame: ItemFrame,
    ): CanvasRayTraceResult? {
        // マップデータを取得、ただの地図ならばスキップ
        val mapItem = MapItem.get(itemFrame.item)
            ?: return null
        // アイテムフレームの位置
        val itemFrameLocation = itemFrame.location
        // キャンバス平面の位置
        val canvasLocation = itemFrameLocation.toCanvasLocation()
        // キャンバスのオフセットを計算
        val canvasIntersectOffset = intersectCanvas(playerEyePos, canvasLocation, itemFrame.isVisible)
        // UVに変換
        val rawUV = mapToBlockUV(itemFrameLocation.yaw, itemFrameLocation.pitch, canvasIntersectOffset)
        // キャンバス内UVを計算、キャンバス範囲外ならばスキップ
        val uv = transformUV(itemFrame.rotation, rawUV)
            ?: return null
        return CanvasRayTraceResult(itemFrame, mapItem, canvasLocation, canvasIntersectOffset, uv)
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
     * @param canvasLocation キャンバス平面の位置
     * @param isFrameVisible アイテムフレームが見えるかどうか
     * @return 交点座標
     */
    private fun intersectCanvas(
        playerEyePos: Location,
        canvasLocation: Location,
        isFrameVisible: Boolean,
    ): Vector {
        // プレイヤーの目線の方向
        val playerDirection = playerEyePos.direction

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
     * (tpでアイテムフレームを回転したときにずれる)
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