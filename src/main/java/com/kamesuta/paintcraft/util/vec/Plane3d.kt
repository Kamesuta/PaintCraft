package com.kamesuta.paintcraft.util.vec

import org.bukkit.util.Vector
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * 平面を表すクラス
 * @param a X座標
 * @param b Y座標
 * @param c Z座標
 * @param d 距離
 */
data class Plane3d(
    val a: Double,
    val b: Double,
    val c: Double,
    val d: Double,
) {
    /**
     * 正規化された法線と距離で初期化する
     */
    constructor(normalizedNormal: Vector, d: Double) : this(
        normalizedNormal.x,
        normalizedNormal.y,
        normalizedNormal.z,
        d
    )

    /**
     * 正規化された平面
     */
    val normalized: Plane3d
        get() {
            val length = sqrt(a * a + b * b + c * c)
            return Plane3d(a / length, b / length, c / length, d / length)
        }

    /**
     * 法線
     */
    val normal: Vector get() = Vector(a, b, c)

    /**
     * 点までの符号付きの最短距離を返す
     * @param point 点
     * @return 点までの符号付きの最短距離
     */
    fun signedDistance(point: Vector): Double {
        return normal.dot(point) + d
    }

    /**
     * 点までの最短距離を返す
     * @param point 点
     * @return 点までの最短距離
     */
    fun distance(point: Vector): Double {
        return abs(signedDistance(point))
    }

    /**
     * 点に一番近い平面上の点を返す
     * @param point 点
     * @return 点に一番近い平面上の点
     */
    fun closestPoint(point: Vector): Vector {
        return point - normal * signedDistance(point)
    }

    /**
     * 平面と線の交点を返す
     * @param ray 線
     * @return 平面と線の交点
     */
    fun intersect(ray: Line3d): Vector? {
        // 平面とベクトルとの交点を求める
        // https://qiita.com/edo_m18/items/c8808f318f5abfa8af1e
        // http://www.sousakuba.com/Programming/gs_plane_line_intersect.html
        val denom = normal.dot(ray.direction)
        if (denom == 0.0) {
            return null
        }
        val t = -(normal.dot(ray.origin) + d) / denom
        return ray.origin + (ray.direction * t)
    }

    /**
     * 平面と平面の交線を返す
     * @param other 平面
     * @return 平面と平面の交線
     */
    fun intersect(other: Plane3d): Line3d? {
        // https://stackoverflow.com/a/32410473
        // 論理的には3つ目の平面を作るが、法線しか利用しない
        val normal1 = normal
        val normal2 = other.normal
        val normal = normal1.getCrossProduct(normal2)
        val det = normal.lengthSquared()

        // detが0の場合は平行な面のため交わらない
        // 正確にはepsilonと比較するべきだが、厳密に平行を区別する必要がないので省略
        if (det == 0.0) {
            return null
        }

        // 平面の座標を計算
        val origin = ((normal.getCrossProduct(normal2) * d) + (normal1.getCrossProduct(normal) * other.d)) / det
        return Line3d(origin, normal)
    }

    /** 平面の右方向のベクトル (Y軸と平面の法線の外積) */
    val right: Vector
        get() {
            // 法線
            val normal = normal
            // Y軸と直交するベクトル (Y軸と法線が平行の場合0ベクトルになる)
            val rightVectorOrZero = normal.getCrossProduct(Vector(0.0, 1.0, 0.0))
            // Y軸と直交するベクトルが0ベクトルの場合はZ軸と直交するベクトルを返す
            val rightVector = when (rightVectorOrZero.lengthSquared() > 0.0) {
                true -> rightVectorOrZero.normalized
                false -> normal.getCrossProduct(Vector(0.0, 0.0, 1.0))
            }
            return rightVector
        }

    /** 平面の上方向のベクトル (法線と右方向ベクトルの外積) */
    val up get() = normal.getCrossProduct(right).normalized

    companion object {
        /**
         * 座標と法線から平面を作成する (正規化済み)
         * @param point 座標
         * @param normal 法線
         * @return 平面
         */
        fun fromPointNormal(point: Vector, normal: Vector) =
            Plane3d(normal, -normal.dot(point)).normalized

        /**
         * 座標と辺から平面を作成する (正規化済み)
         * @param point 座標
         * @param v1 辺1
         * @param v2 辺2
         * @return 平面
         */
        fun fromPointVectors(point: Vector, v1: Vector, v2: Vector) = fromPointNormal(
            point,
            v1.getCrossProduct(v2),
        )

        /**
         * 3つの点から平面を作成する (正規化済み)
         * @param p0 点0
         * @param p1 点1
         * @param p2 点2
         * @return 平面
         */
        fun fromPoints(p0: Vector, p1: Vector, p2: Vector): Plane3d {
            val v1 = p1 - p0
            val v2 = p2 - p0
            val normal = v1.getCrossProduct(v2)
            return fromPointNormal(p0, normal)
        }
    }
}