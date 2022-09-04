package com.kamesuta.paintcraft.frame

import com.kamesuta.paintcraft.canvas.CanvasActionType
import com.kamesuta.paintcraft.canvas.CanvasInteraction
import com.kamesuta.paintcraft.canvas.CanvasSession
import com.kamesuta.paintcraft.map.DrawableMapBuffer.Companion.mapSize
import com.kamesuta.paintcraft.map.DrawableMapItem
import com.kamesuta.paintcraft.util.DebugLocationType
import com.kamesuta.paintcraft.util.DebugLocationVisualizer.debugLocation
import com.kamesuta.paintcraft.util.clienttype.ClientType
import com.kamesuta.paintcraft.util.vec.*
import org.bukkit.Material
import org.bukkit.Rotation
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector

/**
 * キャンバスと目線の交差判定をし、UVを計算します
 * @param player プレイヤー
 * @param clientType クライアントの種類
 */
class FrameRayTrace(
    val player: Player,
    val clientType: ClientType
) {
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
            locate(DebugLocationType.EYE_LOCATION, playerEyePos.origin)
            locate(DebugLocationType.EYE_DIRECTION, playerEyePos.target)
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
            locate(DebugLocationType.BLOCK_HIT_LOCATION, blockHitLocation)
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
            .filter { it.canvasIntersectLocation.distanceSquared(playerEyePos.origin) <= maxDistance * maxDistance }
            // 一番近いヒットしたキャンバス
            .minByOrNull {
                // 距離の2条で比較する
                it.canvasIntersectLocation.distanceSquared(playerEyePos.origin)
            }
            ?: return null

        // 最大距離より遠い場合は除外 (ブロックより後ろのアイテムフレームは除外)
        if (result.canvasIntersectLocation.distanceSquared(playerEyePos.origin) > maxDistance * maxDistance) {
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
        // キャンバス平面の位置
        val canvasLocation = toCanvasLocation(itemFrame)
        player.debugLocation { locate ->
            // アイテムフレームの位置
            locate(DebugLocationType.CANVAS_LOCATION, canvasLocation.origin)
            // アイテムフレームの正面ベクトル
            locate(DebugLocationType.CANVAS_DIRECTION, canvasLocation.target)
        }

        // キャンバスの回転を計算
        val (canvasYaw, canvasPitch) = getCanvasRotation(itemFrame)

        // キャンバスのオフセットを計算
        val canvasIntersectLocation = canvasLocation.toCanvasPlane(itemFrame.isVisible).intersect(playerEyePos)
            ?: return null
        // UVに変換 → キャンバス内UVを計算、キャンバス範囲外ならばスキップ
        val uv = (canvasIntersectLocation - canvasLocation.origin)
            // UVに変換(-0.5～+0.5)
            .mapToBlockUV(canvasYaw, canvasPitch)
            // キャンバス内UV(0～127)を計算、キャンバス範囲外ならばスキップ
            .transformUV(itemFrame.rotation)
            ?: return null
        // レイの結果を返す
        return FrameRayTraceResult(itemFrame, mapItem, canvasLocation, canvasIntersectLocation, uv)
    }

    /**
     * キャンバスの回転を計算
     * @param itemFrame アイテムフレーム
     * @return キャンバスの回転
     */
    fun getCanvasRotation(itemFrame: ItemFrame): Pair<Float, Float> {
        return if (clientType.isPitchRotationSupported) {
            // Java版1.13以降はYaw/Pitchの自由回転をサポートしている
            itemFrame.location.let { it.yaw to it.pitch }
        } else {
            if (clientType.isFacingRotationOnly) {
                // BE版はブロックに沿った回転のみサポートしている
                val dir = Line3d(Vector(), itemFrame.facing.direction)
                dir.yaw to dir.pitch
            } else {
                // Java版1.12以前はYaw回転のみサポートしている、Pitchは常に0
                itemFrame.location.yaw to 0.0f
            }
        }
    }

    /**
     * キャンバスフレームの平面の座標を求める
     * アイテムフレームの座標からキャンバス平面の座標を計算する
     * (tpでアイテムフレームを回転したときにずれる)
     * @receiver アイテムフレームの座標
     * @return キャンバスフレームの平面の座標
     */
    fun toCanvasLocation(itemFrame: ItemFrame): Line3d {
        // キャンバスの回転を計算
        val (canvasYaw, canvasPitch) = getCanvasRotation(itemFrame)
        // ブロックの中心座標
        val centerLocation = itemFrame.location.toCenterLocation()
        // キャンバスの面を合成して座標と向きを返す
        return centerLocation.origin.toCanvasLocation(canvasYaw, canvasPitch)
    }

    companion object {
        /**
         * キャンバスフレームの平面の座標を求める
         * アイテムフレームの座標からキャンバス平面の座標を計算する
         * (tpでアイテムフレームを回転したときにずれる)
         * @receiver キャンバスの中心座標
         * @return キャンバスフレームの平面の座標
         */
        fun Vector.toCanvasLocation(yaw: Float, pitch: Float): Line3d {
            // キャンバスの向き。通常のdirectionとはpitchが反転していることに注意
            // Y軸回転→X軸回転をX軸回転→Y軸回転にするために、一旦単位方向ベクトルに変換
            val dir = Vector(0.0, 0.0, 1.0)
                .rotateAroundY(Math.toRadians(-yaw.toDouble()))
                .rotateAroundX(Math.toRadians(pitch.toDouble()))
            // 中心の座標ををキャンバスの向き方向にずらす
            val origin = this - (dir * 0.5)
            // キャンバスの面を合成して座標と向きを返す
            return Line3d(origin, dir)
        }

        /**
         * キャンバス平面の位置からキャンバスの平面を取得する
         * @receiver キャンバス平面の位置
         * @param isFrameVisible アイテムフレームが見えるかどうか
         * @return キャンバスの平面
         */
        fun Line3d.toCanvasPlane(isFrameVisible: Boolean): Plane3d {
            // キャンバス平面とアイテムフレームの差 = アイテムフレームの厚さ/2
            val canvasOffsetZ = if (isFrameVisible) 0.07 else 0.0075
            // キャンバスの表面の平面の座標 = アイテムフレームエンティティの中心からアイテムフレームの厚さ/2だけずらした位置
            val canvasPlane = this + (direction * canvasOffsetZ)
            // 平面を作成
            return Plane3d.fromPointNormal(canvasPlane.origin, canvasPlane.direction)
        }

        /**
         * 交点座標をキャンバス上のUV座標に変換する
         * UV座標は中央が(0,0)になる
         * @receiver 交点座標
         * @param itemFrameYaw アイテムフレームのYaw角度
         * @param itemFramePitch アイテムフレームのPitch角度
         * @return キャンバス上のUV座標
         */
        fun Vector.mapToBlockUV(
            itemFrameYaw: Float,
            itemFramePitch: Float,
        ): Vec2d {
            // 交点座標を(0,0)を中心に回転し、UV座標(x,-y)に対応するようにする
            val unRotated = clone()
                .rotateAroundX(Math.toRadians(-itemFramePitch.toDouble()))
                .rotateAroundY(Math.toRadians(itemFrameYaw.toDouble()))
            // UV座標を返す (3D座標はYが上ほど大きく、UV座標はYが下ほど大きいため、Yを反転する)
            return Vec2d(unRotated.x, -unRotated.y)
        }

        /**
         * ブロックのUV座標->キャンバスピクセルのUV座標を計算する
         * @receiver ブロックのUV座標
         * @param rotation アイテムフレーム内の地図の回転
         * @return キャンバスピクセルのUV座標
         */
        fun Vec2d.transformUV(rotation: Rotation): Vec2i? {
            // BukkitのRotationからCanvasのRotationに変換する
            val rot: FrameRotation = FrameRotation.fromRotation(rotation)
            // -0.5～0.5の範囲を0.0～1.0の範囲に変換する
            val q = rot.uv(this) + Vec2d(0.5, 0.5)
            // 0～128(ピクセル座標)の範囲に変換する
            val x = (q.x * mapSize).toInt()
            val y = (q.y * mapSize).toInt()
            // 範囲外ならばnullを返す
            if (x >= mapSize || x < 0) return null
            if (y >= mapSize || y < 0) return null
            // 変換した座標を返す
            return Vec2i(x, y)
        }
    }
}