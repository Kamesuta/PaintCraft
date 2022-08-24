package com.kamesuta.paintcraft.util

/**
 * UV座標(小数)を表すクラス、UV座標は0.0から1.0の範囲で指定する。
 * @param u u座標
 * @param v v座標
 */
data class UV(val u: Double, val v: Double)

/**
 * UV座標(整数)を表すクラス、UV座標は0からmapSizeの範囲で指定する。
 * @param u u座標
 * @param v v座標
 */
data class UVInt(val u: Int, val v: Int)

/**
 * UV座標の範囲を表すクラス、UV座標は0からmapSizeの範囲で指定する。
 * 範囲は終点を含む。
 * @param p1 始点
 * @param p2 終点
 */
data class UVIntArea(val p1: UVInt, val p2: UVInt) {
    val width: Int get() = p2.u - p1.u + 1
    val height: Int get() = p2.v - p1.v + 1
}
