package com.kamesuta.paintcraft.frame

import com.kamesuta.paintcraft.canvas.CanvasActionType
import com.kamesuta.paintcraft.canvas.CanvasInteraction
import com.kamesuta.paintcraft.canvas.CanvasSession
import com.kamesuta.paintcraft.map.DrawableMapBuffer.Companion.mapSize
import com.kamesuta.paintcraft.map.DrawableMapItem
import com.kamesuta.paintcraft.util.DebugLocationType
import com.kamesuta.paintcraft.util.DebugLocationVisualizer.debugLocation
import com.kamesuta.paintcraft.util.vec.Line3d
import com.kamesuta.paintcraft.util.vec.Vec2d
import com.kamesuta.paintcraft.util.vec.Vec2i
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Rotation
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector

/**
 * キャンバスと目線の交差判定をし、UVを計算します
 * @param player プレイヤー
 */
class FrameRayTrace(private val player: Player) {
    /**
     * キャンバスが表か判定する
     * @param playerDirection プレイヤーの方向
     * @param canvasLocation アイテムフレーム
     * @return キャンバスが表かどうか
     */
    fun isCanvasFrontSide(playerDirection: Vector, canvasLocation: Line3d): Boolean {
        // 裏からのクリックを判定
        return playerDirection.dot(canvasLocation.direction) <= 0
    }

    /**
     * レイを飛ばしてアイテムフレームを取得
     * @param playerEyePos プレイヤーの目線の位置
     */
    fun rayTraceCanvas(
        playerEyePos: Line3d,
    ): FrameRayTraceResult? {
        // 目線と向きからエンティティを取得し、アイテムフレームかどうかを確認する
        player.debugLocation { locate ->
            locate(DebugLocationType.EYE_LOCATION, playerEyePos.origin.toLocation(player.world))
            locate(DebugLocationType.EYE_DIRECTION, playerEyePos.target.toLocation(player.world))
        }

        // 距離は前方8m(半径4)を範囲にする
        val distance = 8.0
        // 範囲を全方向にmarginずつ拡張
        val margin = 1.0
        // エンティティを取得する範囲のバウンディングボックス
        val box = BoundingBox.of(playerEyePos.origin, 0.0, 0.0, 0.0).expand(playerEyePos.direction, distance)
        // レイキャストを行い、ヒットしたブロックがあればそのブロック座標と目線の位置から範囲の中心座標とサイズを計算する
        val blockRay = player.world.rayTraceBlocks(
            playerEyePos.origin.toLocation(player.world),
            playerEyePos.direction,
            distance + margin
        )
        // クリックがヒットした座標
        val blockHitLocation = blockRay?.hitPosition
        player.debugLocation { locate ->
            locate(DebugLocationType.BLOCK_HIT_LOCATION, blockHitLocation?.toLocation(player.world))
        }

        // キャンバスよりも手前にブロックがあるならば探索終了
        val maxDistance = (blockHitLocation?.distance(playerEyePos.origin) ?: distance)

        // 範囲内にあるすべてのアイテムフレームを取得する
        val result = player.world.getNearbyEntities(box.clone().expand(margin)) { it is ItemFrame }
            .asSequence()
            .map { it as ItemFrame }
            // その中からアイテムフレームを取得する
            .filter { it.item.type == Material.FILLED_MAP }
            // レイを飛ばす
            .mapNotNull { rayTraceCanvasByEntity(playerEyePos, it) }
            .filter { it.canvasIntersectLocation.origin.distanceSquared(playerEyePos.origin) <= maxDistance * maxDistance }
            // 一番近いヒットしたキャンバス
            .minByOrNull {
                // 距離の2条で比較する
                it.canvasIntersectLocation.origin.distanceSquared(playerEyePos.origin)
            }
            ?: return null

        // 最大距離より遠い場合は除外 (ブロックより後ろのアイテムフレームは除外)
        if (result.canvasIntersectLocation.origin.distanceSquared(playerEyePos.origin) > maxDistance * maxDistance) {
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
        ray: FrameRayTraceResult,
        session: CanvasSession,
        actionType: CanvasActionType
    ) {
        // アイテムフレームの位置を取得
        val itemFrameLocation = ray.itemFrame.location
        player.debugLocation { locate ->
            // アイテムフレームの位置
            locate(DebugLocationType.FRAME_LOCATION, itemFrameLocation)
            // アイテムフレームの方向
            locate(
                DebugLocationType.FRAME_DIRECTION,
                itemFrameLocation.clone().add(itemFrameLocation.direction)
            )
            // アイテムフレームのブロック上での方向
            locate(
                DebugLocationType.FRAME_FACING,
                itemFrameLocation.clone().add(ray.itemFrame.facing.direction)
            )
            // アイテムフレームのブロック
            locate(DebugLocationType.FRAME_FACING_BLOCK, itemFrameLocation.toCenterLocation())
            // ヒット位置
            locate(
                DebugLocationType.CANVAS_HIT_LOCATION,
                ray.canvasIntersectLocation.origin.toLocation(player.world)
            )
        }

        // インタラクトオブジェクトを作成
        val interact = CanvasInteraction(ray.uv, ray, player, actionType)

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
        playerEyePos: Line3d,
        itemFrame: ItemFrame,
    ): FrameRayTraceResult? {
        // マップデータを取得、ただの地図ならばスキップ
        val mapItem = DrawableMapItem.get(itemFrame.item)
            ?: return null
        // アイテムフレームの位置
        val itemFrameLocation = itemFrame.location
        // キャンバス平面の位置
        val canvasLocation = itemFrameLocation.toCanvasLocation()
        player.debugLocation { locate ->
            // アイテムフレームの位置
            locate(DebugLocationType.CANVAS_LOCATION, canvasLocation.origin.toLocation(player.world))
            // アイテムフレームの正面ベクトル
            locate(DebugLocationType.CANVAS_DIRECTION, canvasLocation.target.toLocation(player.world))
        }

        // キャンバスのオフセットを計算
        val canvasIntersectOffset = intersectCanvas(playerEyePos, canvasLocation, itemFrame.isVisible)
        // UVに変換
        val rawUV = mapToBlockUV(itemFrameLocation.yaw, itemFrameLocation.pitch, canvasIntersectOffset)
        // キャンバス内UVを計算、キャンバス範囲外ならばスキップ
        val uv = transformUV(itemFrame.rotation, rawUV)
            ?: return null
        return FrameRayTraceResult(itemFrame, mapItem, canvasLocation, canvasIntersectOffset, uv)
    }

    /**
     * ブロックのUV座標->キャンバスピクセルのUV座標を計算する
     * @param rotation アイテムフレーム内の地図の回転
     * @param uv ブロックのUV座標
     * @return キャンバスピクセルのUV座標
     */
    private fun transformUV(rotation: Rotation, uv: Vec2d): Vec2i? {
        // BukkitのRotationからCanvasのRotationに変換する
        val rot: FrameRotation = FrameRotation.fromRotation(rotation)
        // -0.5～0.5の範囲を0.0～1.0の範囲に変換する
        val q = Vec2d(rot.u(uv) + 0.5, rot.v(uv) + 0.5)
        // 0～128(ピクセル座標)の範囲に変換する
        val x = (q.x * mapSize).toInt()
        val y = (q.y * mapSize).toInt()
        // 範囲外ならばnullを返す
        if (x >= mapSize || x < 0) return null
        if (y >= mapSize || y < 0) return null
        // 変換した座標を返す
        return Vec2i(x, y)
    }

    /**
     * プレイヤーの視点とアイテムフレームの位置から交点の座標を計算する
     * @param playerEyePos プレイヤーの目線位置
     * @param canvasLocation キャンバス平面の位置
     * @param isFrameVisible アイテムフレームが見えるかどうか
     * @return 交点座標
     */
    private fun intersectCanvas(
        playerEyePos: Line3d,
        canvasLocation: Line3d,
        isFrameVisible: Boolean,
    ): Vector {
        // プレイヤーの目線の方向
        val playerDirection = playerEyePos.direction

        // アイテムフレームの正面ベクトル
        val canvasDirection = canvasLocation.direction

        // キャンバス平面とアイテムフレームの差 = アイテムフレームの厚さ/2
        val canvasOffsetZ = if (isFrameVisible) 0.07 else 0.0075
        // キャンバスの表面の平面の座標 = アイテムフレームエンティティの中心からアイテムフレームの厚さ/2だけずらした位置
        val canvasPlane = canvasLocation + canvasDirection.clone().multiply(canvasOffsetZ)

        // アイテムフレームから目線へのベクトル
        val canvasPlaneToEye = playerEyePos.origin.clone().subtract(canvasPlane.origin)

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
    ): Vec2d {
        // 交点座標を(0,0)を中心に回転し、UV座標(x,-y)に対応するようにする
        val unRotated = intersectPosition.clone()
            .rotateAroundX(Math.toRadians(-itemFramePitch.toDouble()))
            .rotateAroundY(Math.toRadians(itemFrameYaw.toDouble()))
        // UV座標を返す (3D座標はYが上ほど大きく、UV座標はYが下ほど大きいため、Yを反転する)
        return Vec2d(unRotated.x, -unRotated.y)
    }

    /**
     * キャンバスフレームの平面の座標を求める
     * アイテムフレームの座標からキャンバス平面の座標を計算する
     * (tpでアイテムフレームを回転したときにずれる)
     * @return キャンバスフレームの平面の座標
     */
    private fun Location.toCanvasLocation(): Line3d {
        // キャンバスの向き。通常のdirectionとはpitchが反転していることに注意
        // Y軸回転→X軸回転をX軸回転→Y軸回転にするために、一旦単位方向ベクトルに変換
        val dir = Vector(0.0, 0.0, 1.0)
            .rotateAroundY(Math.toRadians(-yaw.toDouble()))
            .rotateAroundX(Math.toRadians(pitch.toDouble()))
        // 中心の座標ををキャンバスの向き方向にずらす
        val origin = toCenterLocation().toVector().subtract(dir.clone().multiply(0.5))
        // キャンバスの面を合成して座標と向きを返す
        return Line3d(origin, dir)
    }
}