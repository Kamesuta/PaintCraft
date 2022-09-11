package com.kamesuta.paintcraft.frame

import com.kamesuta.paintcraft.map.DrawableMapBuffer
import com.kamesuta.paintcraft.util.clienttype.ClientType
import com.kamesuta.paintcraft.util.vec.*
import org.bukkit.entity.ItemFrame
import org.bukkit.util.Vector
import kotlin.math.round

/**
 * アイテムフレームの座標、回転
 * @param center アイテムフレームのブロックの中心座標
 * @param yaw アイテムフレームのYaw回転
 * @param pitch アイテムフレームのPitch回転
 * @param offsetZ アイテムフレーム表面の平面の座標のZオフセット (= キャンバス平面とアイテムフレームの座標の差 = アイテムフレームの厚さ/2)
 */
class FrameLocation(
    val center: Vector,
    val yaw: Float,
    val pitch: Float,
    val offsetZ: Double,
) {
    /** 前方向の向き */
    val forward = Vector(0.0, 0.0, 1.0).rotate(yaw, pitch)

    /** 上方向の向き */
    val up = Vector(0.0, 1.0, 0.0).rotate(yaw, pitch)

    /** 右方向の向き */
    val right = Vector(1.0, 0.0, 0.0).rotate(yaw, pitch)

    /** アイテムフレームの座標 */
    val origin = center + (forward * (offsetZ - 0.5))

    /** アイテムフレームの平面 */
    val plane = Plane3d.fromPointNormal(origin, forward)

    /** 平面の線分 */
    val location get() = Line3d(origin, forward)

    /**
     * 交点座標をキャンバス上のUV座標に変換する (-0.5～+0.5)
     * UV座標は中央が(0,0)になる
     * @param location 交点座標
     * @return キャンバス上のUV座標 (-0.5～+0.5)
     */
    fun toBlockUv(location: Vector): Vec2d {
        // 交点座標を(0,0)を中心に回転し、UV座標(x,-y)に対応するようにする
        val unRotated = (location - origin).unrotate(yaw, pitch)
        // UV座標を返す (3D座標はYが上ほど大きく、UV座標はYが下ほど大きいため、Yを反転する)
        return Vec2d(unRotated.x, -unRotated.y)
    }

    /**
     * mapToBlockUVの逆変換
     * UV座標を通常の座標に変換する
     * @param blockUv キャンバス上のUV座標 (-0.5～+0.5)
     * @return 交点座標
     */
    fun fromBlockUv(blockUv: Vec2d): Vector {
        // mapToBlockUVの逆変換
        return Vector(blockUv.x, -blockUv.y, 0.0).rotate(yaw, pitch) + origin
    }

    companion object {
        /**
         * キャンバスフレームの平面の座標を求める
         * アイテムフレームの座標からキャンバス平面の座標を計算する
         * (tpでアイテムフレームを回転したときにずれる)
         * @param itemFrame アイテムフレーム
         * @param clientType クライアントの種類
         * @return キャンバスフレームの平面の座標
         */
        fun fromItemFrame(itemFrame: ItemFrame, clientType: ClientType): FrameLocation {
            // キャンバスの回転を計算
            val (canvasYaw, canvasPitch) = getCanvasRotation(itemFrame, clientType)
            // ブロックの中心座標
            val centerLocation = itemFrame.location.toCenterLocation().origin
            // アイテムフレームが透明かどうか
            val isFrameVisible = itemFrame.isVisible || !clientType.isInvisibleFrameSupported
            // キャンバス平面とアイテムフレームの差 = アイテムフレームの厚さ/2
            val canvasOffsetZ = if (isFrameVisible) 0.07 else 0.0075
            // アイテムフレームを構築
            return FrameLocation(centerLocation, canvasYaw, canvasPitch, canvasOffsetZ)
        }

        /**
         * キャンバスの回転を計算
         * @param itemFrame アイテムフレーム
         * @param clientType クライアントの種類
         * @return キャンバスの回転
         */
        private fun getCanvasRotation(itemFrame: ItemFrame, clientType: ClientType): Pair<Float, Float> {
            return if (clientType.isPitchRotationSupported) {
                // Java版1.13以降はYaw/Pitchの自由回転をサポートしている
                itemFrame.location.let { it.yaw to it.pitch }
            } else if (clientType.isFacingRotationOnly) {
                // BE版はブロックに沿った回転のみサポートしている
                val dir = Line3d(Vector(), itemFrame.facing.direction)
                dir.yaw to dir.pitch
            } else {
                // Java版1.12以前はYaw回転のみサポートしている、Pitchは常に0
                itemFrame.location.yaw to 0.0f
            }
        }

        /**
         * YawとPitchで回転する
         * @param yaw Yaw角度
         * @param pitch Pitch角度
         */
        private fun Vector.rotate(yaw: Float, pitch: Float) = clone()
            // Y軸回転→X軸回転をX軸回転→Y軸回転にするために、一旦単位方向ベクトルに変換
            .rotateAroundY(Math.toRadians(-yaw.toDouble()))
            .rotateAroundX(Math.toRadians(pitch.toDouble()))

        /**
         * YawとPitchの回転を戻す
         * @param yaw Yaw角度
         * @param pitch Pitch角度
         */
        private fun Vector.unrotate(yaw: Float, pitch: Float) = clone()
            .rotateAroundX(Math.toRadians(-pitch.toDouble()))
            .rotateAroundY(Math.toRadians(yaw.toDouble()))

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
         * ブロックのUV座標->キャンバスピクセルのUV座標を計算する
         * @receiver ブロックのUV座標
         * @param rotation アイテムフレーム内の地図の回転
         * @return キャンバスピクセルのUV座標
         */
        fun Vec2d.transformUv(rotation: FrameRotation): Vec2i {
            // -0.5～0.5の範囲を0.0～1.0の範囲に変換する
            val q = rotation.uv(this) + Vec2d(0.5, 0.5)
            // 0～128(ピクセル座標)の範囲に変換する
            val x = round(q.x * (DrawableMapBuffer.mapSize - 1)).toInt()
            val y = round(q.y * (DrawableMapBuffer.mapSize - 1)).toInt()
            // 変換した座標を返す
            return Vec2i(x, y)
        }

        /**
         * キャンバスピクセルのUV座標がキャンバス内にあるかどうかを判定する
         * @receiver キャンバスピクセルのUV座標
         * @return キャンバス内にあるかどうか
         */
        fun Vec2i.isUvInMap(): Boolean {
            if (x >= DrawableMapBuffer.mapSize || x < 0) return false
            if (y >= DrawableMapBuffer.mapSize || y < 0) return false
            return true
        }

        /**
         * キャンバスピクセルのUV座標をキャンバスの範囲内に収める
         * @receiver キャンバスピクセルのUV座標
         * @return ClampされたキャンバスピクセルのUV座標
         */
        fun Vec2i.clampUvInMap(): Vec2i {
            return Vec2i(
                x.coerceIn(0, DrawableMapBuffer.mapSize - 1),
                y.coerceIn(0, DrawableMapBuffer.mapSize - 1)
            )
        }

        /**
         * 直線との正方形の線分を計算します
         * @receiver 直線
         * @param range 正方形の半径
         * @return 線分
         */
        @Suppress("LocalVariableName", "FunctionName")
        fun Line2d.clipBlockUV(range: Double = 0.5): Line2d? {
            // 以下の式を使用して交点を計算する
            // https://www.desmos.com/calculator/rqjlphqe2b

            // 座標の計算式
            val A = origin
            val B = target
            fun Y(x: Double) = (B.y - A.y) / (B.x - A.x) * (x - A.x) + A.y
            fun X(y: Double) = (B.x - A.x) / (B.y - A.y) * (y - A.y) + A.x

            // 辺の交点の座標を計算する
            val x0 = X(-range)  // y = -0.5 上辺の交点のx座標
            val x1 = X(range)   // y = 0.5  下辺の交点のx座標
            val y0 = Y(-range)  // x = -0.5 左辺の交点のy座標
            val y1 = Y(range)   // x = 0.5  右辺の交点のy座標

            // 交点をリストにする
            val crossPoints = mutableListOf<Vec2d>()
            if (x0 in -range..range) crossPoints.add(Vec2d(x0, -range))
            if (x1 in -range..range) crossPoints.add(Vec2d(x1, range))
            if (y0 in -range..range) crossPoints.add(Vec2d(-range, y0))
            if (y1 in -range..range) crossPoints.add(Vec2d(range, y1))

            // 交点リストから線分を計算する
            return when (crossPoints.size) {
                1 -> Line2d.fromPoints(crossPoints[0], crossPoints[0])
                2 -> Line2d.fromPoints(crossPoints[0], crossPoints[1])
                else -> null
            }
        }
    }
}