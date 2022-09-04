package com.kamesuta.paintcraft.util.vec

import com.kamesuta.paintcraft.util.vec.debug.DebugLocatable
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
) : DebugLocatable {
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
        val normal = normal1.getCrossProduct(normal2);
        val det = normal.lengthSquared();

        // detが0の場合は平行な面のため交わらない
        // 正確にはepsilonと比較するべきだが、厳密に平行を区別する必要がないので省略
        if (det == 0.0) {
            return null;
        }

        // 平面の座標を計算
        val origin = ((normal.getCrossProduct(normal2) * d) + (normal1.getCrossProduct(normal) * other.d)) / det;
        return Line3d(origin, normal);
    }

    /**
     * デバッグ用に平面を描画する
     */
    override fun debugLocate(eyeLocation: Line3d, locate: (Vector) -> Unit) {
        val norm = normal
        val dirA = Vector(0.0, 1.0, 0.0)
        val dirB = norm.getCrossProduct(dirA)
        val dirC = when (dirB.lengthSquared() > 0.0) {
            true -> dirB.normalized
            false -> norm.getCrossProduct(Vector(1.0, 0.0, 0.0))
        }
        val dirD = norm.getCrossProduct(dirC).normalized

        val closestPoint = closestPoint(eyeLocation.origin)
        for (y in -10..10) {
            for (x in -10..10) {
                val pos = closestPoint + (dirC * (x.toDouble() * 0.5)) + (dirD * (y.toDouble() * 0.5))
                locate(pos)
            }
        }
        for (y in -10..10) {
            for (x in -10..10) {
                val pos = closestPoint + (dirC * (x.toDouble() * 2.0)) + (dirD * (y.toDouble() * 2.0))
                locate(pos)
            }
        }
    }

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