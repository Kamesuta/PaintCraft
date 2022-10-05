package com.kamesuta.paintcraft.util.vec

import kotlin.math.sqrt

/**
 * 3Dベクトル(小数)を表すクラス
 * @param x x座標
 * @param y y座標
 * @param z z座標
 */
data class Vec3d(val x: Double, val y: Double, val z: Double) {
    /** ベクトルの長さの2乗 */
    val lengthSquared: Double get() = x * x + y * y + z * z

    /** ベクトルの長さ */
    val length: Double get() = sqrt(lengthSquared)

    /** ベクトルの正規化 */
    val normalized: Vec3d
        get() {
            val length = length
            return Vec3d(x / length, y / length, z / length)
        }

    /** ベクトルを合成 */
    operator fun plus(other: Vec3d) = Vec3d(x + other.x, y + other.y, z + other.z)

    /** 逆方向ベクトルを合成 */
    operator fun minus(other: Vec3d) = Vec3d(x - other.x, y - other.y, z - other.z)

    /** ベクトルのスカラー倍 */
    operator fun times(other: Double) = Vec3d(x * other, y * other, z * other)

    /** ベクトルの1/スカラー倍 */
    operator fun div(other: Double) = Vec3d(x / other, y / other, z / other)

    /** マイナス */
    operator fun unaryMinus() = Vec3d(-x, -y, -z)

    /**
     * ベクトルの内積
     * @param other 内積を取るベクトル
     * @return 内積
     */
    fun dot(other: Vec3d) = x * other.x + y * other.y + z * other.z

    /**
     * ベクトルの外積
     * @param other 外積を取るベクトル
     * @return 外積
     */
    fun cross(other: Vec3d) = Vec3d(
        y * other.z - other.y * z,
        z * other.x - other.z * x,
        x * other.y - other.x * y,
    )

    /**
     * 2点間の距離の2乗
     * @param other ベクトル
     * @return 距離の2乗
     */
    fun distanceSquared(other: Vec3d) = (x - other.x) * (x - other.x) +
            (y - other.y) * (y - other.y) +
            (z - other.z) * (z - other.z)

    /**
     * 2点間の距離
     * @param other ベクトル
     * @return 距離
     */
    fun distance(other: Vec3d): Double {
        return sqrt(distanceSquared(other))
    }

    companion object {
        /** 原点 */
        val Zero = Vec3d(0.0, 0.0, 0.0)

        /** X軸 */
        val AxisX = Vec3d(1.0, 0.0, 0.0)

        /** Y軸 */
        val AxisY = Vec3d(0.0, 1.0, 0.0)

        /** Z軸 */
        val AxisZ = Vec3d(0.0, 0.0, 1.0)
    }
}
