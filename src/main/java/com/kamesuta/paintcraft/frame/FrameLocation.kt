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
    val forward = Vector(0.0, 0.0, 1.0).rotateYawPitch(yaw, pitch)

    /** 上方向の向き */
    val up = Vector(0.0, 1.0, 0.0).rotateYawPitch(yaw, pitch)

    /** 右方向の向き */
    val right = Vector(1.0, 0.0, 0.0).rotateYawPitch(yaw, pitch)

    /** アイテムフレームの座標 */
    val origin = center + (forward * (offsetZ - 0.5))

    /** アイテムフレームの平面 */
    val plane = Plane3d.fromPointNormal(origin, forward)

    /** 平面の線分 */
    val location get() = Line3d(origin, forward)

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
         */
        private fun Vector.rotateYawPitch(yaw: Float, pitch: Float): Vector {
            // Y軸回転→X軸回転をX軸回転→Y軸回転にするために、一旦単位方向ベクトルに変換
            return clone()
                .rotateAroundY(Math.toRadians(-yaw.toDouble()))
                .rotateAroundX(Math.toRadians(pitch.toDouble()))
        }

        /**
         * 交点座標をキャンバス上のUV座標に変換する
         * UV座標は中央が(0,0)になる
         * @receiver 交点座標
         * @param itemFrameYaw アイテムフレームのYaw角度
         * @param itemFramePitch アイテムフレームのPitch角度
         * @return キャンバス上のUV座標
         */
        fun Vector.mapLocationToBlockUv(
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
         * mapToBlockUVの逆変換
         * UV座標を通常の座標に変換する
         * @receiver キャンバス上のUV座標
         * @param itemFrameYaw アイテムフレームのYaw角度
         * @param itemFramePitch アイテムフレームのPitch角度
         * @return 交点座標
         */
        fun Vec2d.mapBlockUvToLocation(
            itemFrameYaw: Float,
            itemFramePitch: Float,
        ): Vector {
            // mapToBlockUVの逆変換
            return Vector(x, -y, 0.0)
                .rotateAroundY(Math.toRadians(-itemFrameYaw.toDouble()))
                .rotateAroundX(Math.toRadians(itemFramePitch.toDouble()))
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
    }
}