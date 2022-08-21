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
