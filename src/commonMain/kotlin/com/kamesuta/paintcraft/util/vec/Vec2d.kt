package com.kamesuta.paintcraft.util.vec

import kotlin.math.sqrt

/**
 * 2Dベクトル(小数)を表すクラス
 * @note キャンバスで使用する場合: UV座標として用いる。UV座標は0.0から1.0の範囲で指定する。
 * @param x u座標
 * @param y v座標
 */
data class Vec2d(val x: Double, val y: Double) {
    /** ベクトルの長さの2乗 */
    val lengthSquared: Double get() = x * x + y * y

    /** ベクトルの長さ */
    val length: Double get() = sqrt(lengthSquared)

    /** ベクトルの正規化 */
    val normalized: Vec2d
        get() {
            val length = length
            return Vec2d(x / length, y / length)
        }

    /** ベクトルを合成 */
    operator fun plus(other: Vec2d) = Vec2d(x + other.x, y + other.y)

    /** 逆方向ベクトルを合成 */
    operator fun minus(other: Vec2d) = Vec2d(x - other.x, y - other.y)

    /** ベクトルのスカラー倍 */
    operator fun times(other: Double) = Vec2d(x * other, y * other)

    /** ベクトルの1/スカラー倍 */
    operator fun div(other: Double) = Vec2d(x / other, y / other)

    /**
     * ベクトルの内積
     * @param other 内積を取るベクトル
     * @return 内積
     */
    fun dot(other: Vec2d) = x * other.x + y * other.y

    /**
     * ベクトルの外積
     * @param other 外積を取るベクトル
     * @return 外積
     */
    fun cross(other: Vec2d) = x * other.y - y * other.x
}
