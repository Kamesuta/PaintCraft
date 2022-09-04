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
data class Line3d(val origin: Vector, val direction: Vector) : DebugLocatable {
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
        val v = point - origin;
        val d = v.dot(dir);
        return origin + dir * d;
    }

    /**
     * デバッグ用に線を描画する
     */
    override fun debugLocate(eyeLocation: Line3d, locate: (Vector) -> Unit) {
        val dir = direction.normalized
        val closestPoint = closestPoint(eyeLocation.origin)
        for (i in -10..10) {
            val pos = closestPoint + (dir * (i.toDouble() * 0.5))
            locate(pos)
        }
        for (i in -10..10) {
            val pos = closestPoint + (dir * (i.toDouble() * 4.0))
            locate(pos)
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
