package com.kamesuta.paintcraft.util.vec

import kotlin.math.*

/**
 * クォータニオン(四次数)回転
 * @param x x成分
 * @param y y成分
 * @param z z成分
 * @param w w成分
 */
class Quaternion3d(val x: Double, val y: Double, val z: Double, val w: Double) {
    /**
     * X, Y, Zの成分を取得する
     * @return X, Y, Zの成分
     */
    val complex: Vec3d get() = Vec3d(x, y, z)

    /**
     * 共役クォータニオン
     * @return 共役クォータニオン
     */
    val conjugate get() = Quaternion3d(-x, -y, -z, w)

    /**
     * 逆クォータニオンを求める
     * もし正規化されていると分かっている場合はconjugateを使うと速い
     * @return 逆クォータニオン
     */
    val inverse get() = conjugate / length

    /**
     * 回転の合成
     * @param other 合成する回転
     * @return 合成された回転
     */
    operator fun times(other: Quaternion3d) = product(other)

    /**
     * 回転の合成
     * @param other 合成する回転
     * @return 合成された回転
     */
    fun product(other: Quaternion3d) = Quaternion3d(
        w * other.x + x * other.w + y * other.z - z * other.y,
        w * other.y + y * other.w + z * other.x - x * other.z,
        w * other.z + z * other.w + x * other.y - y * other.x,
        w * other.w - x * other.x - y * other.y - z * other.z
    )

    /**
     * 成分をスカラー倍する
     * @param scale スカラー
     * @return スカラー倍されたクォータニオン
     */
    operator fun times(scale: Double) = Quaternion3d(x * scale, y * scale, z * scale, w * scale)

    /**
     * 成分を1/スカラー倍する
     * @param scale スカラー
     * @return 1/スカラー倍されたクォータニオン
     */
    operator fun div(scale: Double) = Quaternion3d(x / scale, y / scale, z / scale, w / scale)

    /**
     * 成分を加算する
     * @param other 加算するクォータニオン
     * @return 加算されたクォータニオン
     */
    operator fun plus(other: Quaternion3d) = Quaternion3d(x + other.x, y + other.y, z + other.z, w + other.w)

    /**
     * 成分を減算する
     * @param other 減算するクォータニオン
     * @return 減算されたクォータニオン
     */
    operator fun minus(other: Quaternion3d) = Quaternion3d(x - other.x, y - other.y, z - other.z, w - other.w)

    /**
     * 成分を反転する
     * @return 成分が反転されたクォータニオン
     */
    operator fun unaryMinus() = Quaternion3d(-x, -y, -z, -w)

    /**
     * クォータニオンの長さを求める
     * @return クォータニオンの長さ
     */
    val length get() = sqrt(lengthSquared)

    /**
     * クォータニオンの長さの2乗を求める
     * @return クォータニオンの長さの2乗
     */
    val lengthSquared get() = x * x + y * y + z * z + w * w

    /**
     * 正規化されたクォータニオン
     */
    val normalized: Quaternion3d
        get() {
            val length = sqrt(x * x + y * y + z * z + w * w)
            return Quaternion3d(x / length, y / length, z / length, w / length)
        }

    /**
     * 座標を回転する
     * @param vec 回転する座標ベクトル
     * @return 回転したベクトル
     */
    fun transform(vec: Vec3d) = ((this * Quaternion3d(vec.x, vec.y, vec.z, 0.0)) * conjugate).complex;

    /**
     * 球面補間
     * @param other 移動先のクォータニオン
     * @param t 時間
     * @return 球面補間されたクォータニオン
     */
    fun slerp(other: Quaternion3d, t: Double): Quaternion3d {
        var cosHalfTheta = w * other.w + x * other.x + y * other.y + z * other.z
        if (cosHalfTheta < 0) {
            cosHalfTheta = -cosHalfTheta
        }
        if (cosHalfTheta >= 1.0) {
            return this
        }
        val halfTheta = acos(cosHalfTheta)
        var sinHalfTheta = sqrt(1.0 - cosHalfTheta * cosHalfTheta)
        if (abs(sinHalfTheta) < 0.001) {
            sinHalfTheta = 0.001
        }
        val ratioA = sin((1 - t) * halfTheta) / sinHalfTheta
        val ratioB = sin(t * halfTheta) / sinHalfTheta
        return Quaternion3d(
            (x * ratioA + other.x * ratioB),
            (y * ratioA + other.y * ratioB),
            (z * ratioA + other.z * ratioB),
            (w * ratioA + other.w * ratioB)
        )
    }

    companion object {
        /**
         * オイラー角からクォータニオンを生成する
         * @param x X軸回転
         * @param y Y軸回転
         * @param z Z軸回転
         * @return クォータニオン
         */
        fun euler(x: Double, y: Double, z: Double): Quaternion3d {
            val c1 = cos(x / 2)
            val c2 = cos(y / 2)
            val c3 = cos(z / 2)
            val s1 = sin(x / 2)
            val s2 = sin(y / 2)
            val s3 = sin(z / 2)
            return Quaternion3d(
                c1 * c2 * s3 + s1 * s2 * c3,
                c1 * s2 * c3 + s1 * c2 * s3,
                s1 * c2 * c3 - c1 * s2 * s3,
                c1 * c2 * c3 - s1 * s2 * s3
            )
        }

        /**
         * 軸と角度からクォータニオンを生成する
         * @param axis 軸
         * @param angle 角度
         * @return クォータニオン
         */
        fun axisAngle(axis: Vec3d, angle: Double): Quaternion3d {
            val vn = axis.normalized
            val sinAngle = sin(angle / 2)
            val cosAngle = cos(angle / 2)
            return Quaternion3d(vn.x * sinAngle, vn.y * sinAngle, vn.z * sinAngle, cosAngle)
        }

        /**
         * X軸回転クォータニオン
         * @param angle 角度
         * @return X軸回転クォータニオン
         */
        fun rotateX(angle: Double) = axisAngle(Vec3d.AxisX, angle)

        /**
         * Y軸回転クォータニオン
         * @param angle 角度
         * @return Y軸回転クォータニオン
         */
        fun rotateY(angle: Double) = axisAngle(Vec3d.AxisY, angle)

        /**
         * Z軸回転クォータニオン
         * @param angle 角度
         * @return Z軸回転クォータニオン
         */
        fun rotateZ(angle: Double) = axisAngle(Vec3d.AxisZ, angle)
    }
}