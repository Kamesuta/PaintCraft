package com.kamesuta.paintcraft.util.vec

import com.kamesuta.paintcraft.util.fuzzyEq
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * クォータニオン(四次数)回転
 * @param x x成分
 * @param y y成分
 * @param z z成分
 * @param w w成分
 */
data class Quaternion3d(val x: Double, val y: Double, val z: Double, val w: Double) {
    /**
     * X, Y, Zの成分を取得する
     * @return X, Y, Zの成分
     */
    val complex: Vec3d get() = Vec3d(x, y, z)

    /**
     * 共役クォータニオン (回転は逆になる)
     * @return 共役クォータニオン
     */
    val conjugate get() = Quaternion3d(-x, -y, -z, w)

    /**
     * 成分を反転する (回転は全く同じ)
     * @return 成分が反転されたクォータニオン
     */
    val negate get() = Quaternion3d(-x, -y, -z, -w)

    /**
     * 成分を反転する (回転は全く同じ)
     * @return 成分が反転されたクォータニオン
     */
    operator fun unaryMinus() = negate

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
    val normalized: Quaternion3d get() = this / length

    /**
     * 逆クォータニオンを求める (回転は逆になる)
     * もし正規化されていると分かっている場合はconjugateを使うと速い
     * @return 逆クォータニオン
     */
    val inverse: Quaternion3d get() = conjugate.normalized

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
     * ドット積
     * @param other ドット積を取るクォータニオン
     * @return ドット積
     */
    fun dot(other: Quaternion3d) = x * other.x + y * other.y + z * other.z + w * other.w

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
     * 回転をスカラー倍する
     * @param scale スカラー
     * @return スカラー倍回転されたクォータニオン
     */
    fun scaleRotation(scale: Double): Quaternion3d {
        val (axis, angle) = toAxisAngle()
        return axisAngle(axis, angle * scale)
    }

    /**
     * 座標を回転する
     * @param vec 回転する座標ベクトル
     * @return 回転したベクトル
     */
    fun transform(vec: Vec3d) = ((this * Quaternion3d(vec.x, vec.y, vec.z, 0.0)) * conjugate).complex

    /**
     * 軸と角度に変換する
     * @return 軸と角度
     */
    fun toAxisAngle(): Pair<Vec3d, Double> {
        val q = (if (w > 0) this else negate).normalized
        val axis = q.complex
        val axisLength = axis.length
        val outAxis = if (axisLength fuzzyEq 0.0) {
            // 0度と360度のときはどの軸でも正しいので、X軸を返す
            Vec3d.AxisX
        } else {
            axis.normalized
        }
        val outAngle = 2.0 * atan2(axisLength, q.w)
        return outAxis to outAngle
    }

    companion object {
        /** 単位クォータニオン */
        val Identity = Quaternion3d(0.0, 0.0, 0.0, 1.0)

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

        /**
         * 球面補間
         * @param a 移動元のクォータニオン
         * @param b 移動先のクォータニオン
         * @param t 時間
         * @return 球面補間されたクォータニオン
         */
        fun slerp(a: Quaternion3d, b: Quaternion3d, t: Double): Quaternion3d {
            if (a.dot(b) > 0.9999) {
                // ほぼ同じ姿勢なので、線形補間をする
                return (a * (1 - t) + b * t).normalized
            }
            // 球面補間
            return (a * (a.inverse * b).scaleRotation(t))
        }
    }
}