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
class Plane3d(
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
     * 正規化する
     * @return 正規化された平面
     */
    fun normalize(): Plane3d {
        val length = sqrt(a * a + b * b + c * c)
        return Plane3d(a / length, b / length, c / length, d / length)
    }

    /**
     * 点までの符号付きの最短距離を返す
     * @param point 点
     * @return 点までの符号付きの最短距離
     */
    fun signedDistance(point: Vector): Double {
        return a * point.x + b * point.y + c * point.z + d
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
        return point.clone().add(normal().multiply(signedDistance(point)))
    }

    /**
     * 法線を返す
     * @return 法線
     */
    private fun normal() = Vector(a, b, c)

    /**
     * 平面と線の交点を返す
     * @param ray 線
     * @return 平面と線の交点
     */
    fun intersect(ray: Line3d): Vector? {
        // 平面とベクトルとの交点を求める
        // https://qiita.com/edo_m18/items/c8808f318f5abfa8af1e
        // http://www.sousakuba.com/Programming/gs_plane_line_intersect.html
        val denom = a * ray.direction.x + b * ray.direction.y + c * ray.direction.z
        if (denom == 0.0) {
            return null
        }
        val t = -(a * ray.origin.x + b * ray.origin.y + c * ray.origin.z + d) / denom
        return ray.origin.clone().add(ray.direction.clone().multiply(t))
    }

    /**
     * 平面と平面の交線を返す
     * @param other 平面
     * @return 平面と平面の交線
     */
    fun intersect(other: Plane3d): Line3d? {
        val denom = a * other.b + b * other.a
        if (denom == 0.0) {
            // 2つの軸が交わったときに発生する
            return null
        }
        val origin = Vector(
            (other.d * b - d * other.b) / denom,
            (d * other.a - other.d * a) / denom,
            0.0
        )
        val direction = normal().crossProduct(other.normal())
        if (direction.length() == 0.0) {
            // 2つの面が平行であるときに発生する
            return null
        }
        return Line3d(origin, direction.normalize())
    }

    companion object {
        /**
         * 座標と法線から平面を作成する
         * @param point 座標
         * @param normal 法線
         * @return 平面
         */
        fun fromPointNormal(point: Vector, normal: Vector) =
            Plane3d(normal.clone().normalize(), -normal.dot(point))

        /**
         * 座標と辺から平面を作成する
         * @param point 座標
         * @param v1 辺1
         * @param v2 辺2
         * @return 平面
         */
        fun fromPointVectors(point: Vector, v1: Vector, v2: Vector) = fromPointNormal(
            point,
            v1.clone().crossProduct(v2),
        )

        /**
         * 3つの点から平面を作成する
         * @param p0 点0
         * @param p1 点1
         * @param p2 点2
         * @return 平面
         */
        fun fromPoints(p0: Vector, p1: Vector, p2: Vector): Plane3d {
            val v1 = p1.clone().subtract(p0)
            val v2 = p2.clone().subtract(p0)
            val normal = v1.clone().crossProduct(v2).normalize()
            return fromPointNormal(p0, normal)
        }
    }
}