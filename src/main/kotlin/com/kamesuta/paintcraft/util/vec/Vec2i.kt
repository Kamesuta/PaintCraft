package com.kamesuta.paintcraft.util.vec

/**
 * 2Dベクトル(整数)を表すクラス
 * @note キャンバスで使用する場合: UV座標として用いる。UV座標は0からmapSizeの範囲で指定する。
 * @param x x座標
 * @param y v座標
 */
data class Vec2i(val x: Int, val y: Int) {
    /** ベクトルを合成 */
    operator fun plus(other: Vec2i) = Vec2i(x + other.x, y + other.y)

    /** 逆方向ベクトルを合成 */
    operator fun minus(other: Vec2i) = Vec2i(x - other.x, y - other.y)
}
