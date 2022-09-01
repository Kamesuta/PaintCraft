package com.kamesuta.paintcraft.util.vec

/**
 * 2D座標上の矩形の範囲を表すクラス
 * @note キャンバスで使用する場合: UV座標は0からmapSizeの範囲で指定し、範囲は終点を含む。
 * @param p1 始点
 * @param p2 終点
 */
data class Rect2i(val p1: Vec2i, val p2: Vec2i) {
    val width: Int get() = p2.x - p1.x + 1
    val height: Int get() = p2.y - p1.y + 1
}
