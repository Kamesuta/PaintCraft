package com.kamesuta.paintcraft.util.vec

import kotlin.math.asin
import kotlin.math.atan2

/**
 * 線を表現するクラス
 * 角度ではなく、方向ベクトルで表現する
 * 長さは常に1ではない
 * @param origin 始点
 * @param direction 方向
 */
data class Line3d(val origin: Vec3d, val direction: Vec3d) {
    /** 線の先の位置 */
    val target: Vec3d get() = origin + direction

    /** 線の長さを1にした線を返す */
    val normalized: Line3d get() = Line3d(origin, direction.normalized)

    /** 平行移動させた線を取得する */
    operator fun plus(other: Vec3d) = Line3d(origin + other, direction)

    /** 逆方向に平行移動させた線を取得する */
    operator fun minus(other: Vec3d) = Line3d(origin - other, direction)

    /** 方向ベクトルからyawを求める */
    val pitch get() = Math.toDegrees(asin(-direction.y)).toFloat()

    /** 方向ベクトルからpitchを求める */
    val yaw get() = Math.toDegrees(-atan2(direction.x, direction.z)).toFloat()

    /**
     * 点と直線の距離の2乗を求める
     * @param point 点
     */
    fun distanceSquared(point: Vec3d) = (point - origin).cross(direction).lengthSquared

    /**
     * 点と直線の距離を求める
     * @param point 点
     */
    fun distance(point: Vec3d) = (point - origin).cross(direction).length

    /**
     * 点に一番近い直線上の点までの符号付き距離
     * closestPoint = closestPointSignedDistance * direction.normalized
     * @param point 点
     * @return 点に一番近い直線上の点までの距離
     */
    fun closestPointSignedDistance(point: Vec3d): Double {
        val dir = direction.normalized
        val v = point - origin
        return v.dot(dir)
    }

    /**
     * 点に一番近い直線上の点を返す
     * @param point 点
     * @return 点に一番近い直線上の点
     */
    fun closestPoint(point: Vec3d): Vec3d {
        val dir = direction.normalized
        val v = point - origin
        val d = v.dot(dir)
        return origin + dir * d
    }

    /**
     * 線分をこの直線上に射影したときの、線分を返す
     * @param segment 線分
     * @return 線分をこの直線上に射影したときの、線分
     */
    fun closestSegment(segment: Line3d): Line3d {
        val p1 = closestPoint(segment.origin)
        val p2 = closestPoint(segment.target)
        return fromPoints(p1, p2)
    }

    companion object {
        /**
         * 始点と終点から線を作成する
         * @param origin 始点
         * @param target 終点
         */
        fun fromPoints(origin: Vec3d, target: Vec3d): Line3d {
            return Line3d(origin, target - origin)
        }
    }
}
