package com.kamesuta.paintcraft.util.vec

import com.kamesuta.paintcraft.util.vec.debug.DebugLocatable
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.util.Vector
import kotlin.math.asin
import kotlin.math.atan2

/**
 * 線を表現するクラス
 * 角度ではなく、方向ベクトルで表現する
 * 長さは常に1ではない
 * @param origin 始点
 * @param direction 方向
 */
data class Line3d(val origin: Vector, val direction: Vector) {
    /** 線の先の位置 */
    val target: Vector get() = origin + direction

    /** 線の長さを1にした線を返す */
    val normalized: Line3d get() = Line3d(origin, direction.normalized)

    /** 平行移動させた線を取得する */
    operator fun plus(other: Vector) = Line3d(origin + other, direction)

    /** 逆方向に平行移動させた線を取得する */
    operator fun minus(other: Vector) = Line3d(origin - other, direction)

    /** 方向ベクトルからyawを求める */
    val pitch get() = Math.toDegrees(asin(-direction.y)).toFloat()

    /** 方向ベクトルからpitchを求める */
    val yaw get() = Math.toDegrees(-atan2(direction.x, direction.z)).toFloat()

    /**
     * 点に一番近い直線上の点を返す
     * @param point 点
     * @return 点に一番近い直線上の点
     */
    fun closestPoint(point: Vector): Vector {
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

    /** デバッグ用の線のタイプ */
    enum class DebugLineType {
        /** 直線 */
        LINE,

        /** 半直線 */
        DIRECTION,

        /** 線分 */
        SEGMENT,
    }

    /**
     * デバッグ用に線を作成する
     * @param lineType 線のタイプ
     */
    fun toDebug(lineType: DebugLineType = DebugLineType.LINE): DebugLocatable = DebugLine(lineType)

    /** デバッグ用の線 */
    private inner class DebugLine(val lineType: DebugLineType) : DebugLocatable {
        /** デバッグ用に線を描画する */
        override fun debugLocate(eyeLocation: Line3d, locate: (Vector) -> Unit) {
            // 単位ベクトル
            val dir = direction.normalized
            // 最短距離の点を求めるためのdを求める (詳しくはclosestPoint()を参照)
            // closestPoint = origin + dir * d
            val v = eyeLocation.origin - origin
            val d = v.dot(dir)
            // 線分の長さ
            val length = direction.length()
            // 線の種類に応じて、範囲内にあれば表示する
            fun locateLinePoint(t: Double) {
                if (d + t < 0 && lineType != DebugLineType.LINE) return
                if (d + t > length && lineType == DebugLineType.SEGMENT) return
                locate(origin + dir * (d + t))
            }
            // 始点と終点を表示する
            locate(origin)
            locate(target)
            // 目線に近い場所を表示する
            for (i in -10..10) locateLinePoint(i.toDouble() * 0.5)
            for (i in -10..10) locateLinePoint(i.toDouble() * 4.0)
        }
    }

    /**
     * BukkitのLocationに変換する
     * @param world ワールド
     * @return Location座標
     */
    @Deprecated("Locationに変換するのはコストが高いため、Line3dのまま使用することを推奨します")
    fun toLocation(world: World): Location {
        // 始点を取得
        val location = origin.toLocation(world)
        // 方向ベクトルからyawとpitchを求める
        location.yaw = yaw
        location.pitch = pitch
        return location
    }

    companion object {
        /**
         * 始点と終点から線を作成する
         * @param origin 始点
         * @param target 終点
         */
        fun fromPoints(origin: Vector, target: Vector): Line3d {
            return Line3d(origin, target - origin)
        }

        /**
         * 線に変換する
         * @return 線
         */
        fun Location.toLine(): Line3d {
            return Line3d(origin, direction)
        }
    }
}
