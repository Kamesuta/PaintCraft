package com.kamesuta.paintcraft.util.vec

/**
 * 線を表現するクラス
 * 角度ではなく、方向ベクトルで表現する
 * 長さは常に1ではない
 * @param origin 始点
 * @param direction 方向
 */
data class Line2d(val origin: Vec2d, val direction: Vec2d) {
    /** 線の先の位置 */
    val target: Vec2d get() = origin + direction

    /** 線の長さを1にした線を返す */
    val normalized: Line2d get() = Line2d(origin, direction.normalized)

    /** 平行移動させた線を取得する */
    operator fun plus(other: Vec2d) = Line2d(origin + other, direction)

    /** 逆方向に平行移動させた線を取得する */
    operator fun minus(other: Vec2d) = Line2d(origin - other, direction)

    companion object {
        /**
         * 始点と終点から線を作成する
         * @param origin 始点
         * @param target 終点
         */
        fun fromPoints(origin: Vec2d, target: Vec2d): Line2d {
            return Line2d(origin, target - origin)
        }
    }
}
