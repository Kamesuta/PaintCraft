package com.kamesuta.paintcraft.util.vec

import kotlin.math.max
import kotlin.math.min

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

    /**
     * 同一直線状にある2つの線分が交差している部分の線分を取得する
     * @param other 内積を取る対象
     */
    fun intersectSegment(other: Line2d): Line2d? {
        // 単位ベクトル
        val dir = direction.normalized
        // 線同士が混じり合う点の候補
        val t0 = 0.0 // A線の始点
        val t1 = dir.dot(direction) // A線の終点
        val s0 = dir.dot(other.origin - origin) // B線の始点
        val s1 = dir.dot(other.target - origin) // B線の終点
        // s0 < s1 になるように入れ替える (t0, t1 は必ず t0 < t1 になる)
        val u0 = min(s0, s1)
        val u1 = max(s0, s1)
        // 交差しているかどうか
        val intersect = (s0 in t0..t1) || (s1 in t0..t1) || (t0 in u0..u1) || (t1 in u0..u1)
        if (!intersect) return null // 交差していない
        // ソートし、内側の2点から線分を作る
        return fromPoints(origin + dir * max(t0, u0), origin + dir * min(t1, u1))
    }

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
