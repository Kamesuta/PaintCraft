package com.kamesuta.paintcraft.util.vec

import org.bukkit.util.Vector

/**
 * 2Dベクトル(小数)を表すクラス
 * @note キャンバスで使用する場合: UV座標として用いる。UV座標は0.0から1.0の範囲で指定する。
 * @param x u座標
 * @param y v座標
 */
data class Vec2d(val x: Double, val y: Double) {
    /**
     * ベクトルを合成
     * @param other ベクトル
     * @return 合成後のベクトル
     */
    operator fun plus(other: Vec2d) = Vec2d(x + other.x, y + other.y)

    /**
     * 逆方向ベクトルを合成
     * @param other ベクトル
     * @return 合成後のベクトル
     */
    operator fun minus(other: Vec2d) = Vec2d(x - other.x, y - other.y)
}
