package com.kamesuta.paintcraft.util.vec

import kotlin.math.max
import kotlin.math.min

/**
 * 3D座標上の矩形の範囲を表すクラス
 * @note キャンバスで使用する場合: UV座標は0からmapSizeの範囲で指定し、範囲は終点を含む。
 * @param min 始点
 * @param max 終点
 */
class Rect3d(val min: Vec3d, val max: Vec3d) {
    /**
     * 範囲を拡大する
     * @param expansion 拡大する範囲
     * @return 拡大した範囲
     */
    fun expand(expansion: Double) = Rect3d(min - Vec3d.One * expansion, max + Vec3d.One * expansion)

    companion object {
        /**
         * 2点を含む範囲を作成
         * @param p1 1つ目の点
         * @param p2 2つ目の点
         * @return 2点を含む範囲
         */
        fun of(p1: Vec3d, p2: Vec3d): Rect3d {
            val minX = min(p1.x, p2.x)
            val minY = min(p1.y, p2.y)
            val minZ = min(p1.z, p2.z)
            val maxX = max(p1.x, p2.x)
            val maxY = max(p1.y, p2.y)
            val maxZ = max(p1.z, p2.z)
            return Rect3d(Vec3d(minX, minY, minZ), Vec3d(maxX, maxY, maxZ))
        }
    }
}