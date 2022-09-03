package com.kamesuta.paintcraft.util.vec

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
        location.yaw = Math.toDegrees(-atan2(direction.x, direction.z)).toFloat()
        location.pitch = Math.toDegrees(asin(-direction.y)).toFloat()
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
            return Line3d(toVector(), direction.normalized)
        }
    }
}
