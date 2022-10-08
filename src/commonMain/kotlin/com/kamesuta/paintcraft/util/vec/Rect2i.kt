package com.kamesuta.paintcraft.util.vec

/**
 * 2D座標上の矩形の範囲を表すクラス
 * @note キャンバスで使用する場合: UV座標は0からmapSizeの範囲で指定し、範囲は終点を含む。
 * @param min 始点
 * @param max 終点
 */
data class Rect2i(val min: Vec2i, val max: Vec2i) {
    /** 幅 */
    val width: Int get() = max.x - min.x + 1

    /** 高さ */
    val height: Int get() = max.y - min.y + 1
}
